package com.github.dcysteine.nesql.exporter.common;

import com.github.dcysteine.nesql.exporter.main.Log;
import com.google.auto.value.AutoValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Mob指定用のデータクラス
 *
 * SQLエンティティと区別するため、MinecraftのEntityを "mob" と呼ぶ
 * {@link NBTTagCompound} がミュータブルなため {@link AutoValue} 契約に完全準拠ではない
 */
@AutoValue
public abstract class MobSpec {
    /**
     * NBTなしでMobSpecを生成する
     *
     * @param mobName Mob名
     * @return MobSpec
     */
    public static MobSpec create(String mobName) {
        return create(mobName, null);
    }

    /**
     * MobSpecを生成する
     *
     * @param mobName Mob名、形式は "modId.MobName" または "MobName"
     * @param nbt NBTデータ、省略時はnull
     * @return MobSpec
     */
    public static MobSpec create(String mobName, @Nullable NBTTagCompound nbt) {
        String modId = "minecraft";
        String shortName = mobName;

        int separator = mobName.indexOf('.');
        if (separator > 0) {
            modId = mobName.substring(0, separator);
            shortName = mobName.substring(separator + 1);
        }
        // separator == -1 の場合はバニラMob、デフォルト値のまま
        return new AutoValue_MobSpec(modId, shortName, mobName, Optional.ofNullable(nbt));
    }

    /**
     * Mod IDを取得する
     *
     * @return Mod ID、バニラMobは "minecraft"
     */
    public abstract String getModId();

    /**
     * Mod IDを除いた内部名を取得する
     * バニラMobの場合はフルネームと同一
     *
     * @return 短縮名
     */
    public abstract String getShortName();

    /**
     * Mod IDを含む完全な内部名を取得する
     * Mob生成時に使用する
     *
     * @return フルネーム
     */
    public abstract String getFullName();

    /**
     * NBTデータを取得する
     *
     * @return NBTデータ
     */
    public abstract Optional<NBTTagCompound> getNbt();

    /**
     * このMobSpecからEntityを生成する
     *
     * @return 生成したEntity、失敗時は空
     */
    public Optional<Entity> createEntity() {
        try {
            Entity entity =
                    EntityList.createEntityByName(getFullName(), Minecraft.getMinecraft().theWorld);
            if (entity == null) {
                Log.MOD.warn("Got null while creating entity: {}", this);
                return Optional.empty();
            }

            getNbt().ifPresent(entity::readFromNBT);
            return Optional.of(entity);
        } catch (Exception e) {
            Log.MOD.error("Caught exception while creating entity: {}", this);
            e.printStackTrace();

            return Optional.empty();
        }
    }
}
