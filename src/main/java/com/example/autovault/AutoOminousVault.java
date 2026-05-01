package com.example.autovault;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.VaultState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.VaultBlockEntity;

public class AutoOminousVault implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Ключевое изменение: используем getEntityWorld() для 1.21.10
                if (!player.getEntityWorld().isClient() && player.isAlive()) {
                    checkAndOpenOminousVault(player);
                }
            }
        });
    }

    private void checkAndOpenOminousVault(PlayerEntity player) {
        ItemStack mainHandItem = player.getMainHandStack();
        if (!mainHandItem.isOf(Items.OMINOUS_TRIAL_KEY)) {
            return;
        }

        World world = player.getEntityWorld();
        BlockPos playerPos = player.getBlockPos();
        int searchRadius = 4;

        BlockPos.streamOutwards(playerPos, searchRadius, searchRadius, searchRadius)
                .forEach(pos -> {
                    BlockState state = world.getBlockState(pos);
                    if (state.getBlock() instanceof VaultBlock && state.get(VaultBlock.STATE) == VaultState.ACTIVE) {
                        BlockEntity blockEntity = world.getBlockEntity(pos);

                        if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                            if (isOminousVaultWithHeavyCore(vaultEntity, (ServerWorld) world)) {
                                boolean opened = VaultBlockEntity.Server.tryInsertKey(
                                        (ServerWorld) world,
                                        pos,
                                        state,
                                        vaultEntity.getConfig(),
                                        vaultEntity.getServerData(),
                                        vaultEntity.getSharedData(),
                                        player,
                                        mainHandItem
                                );

                                if (opened) {
                                    System.out.println("[AutoOminousVault] Ominous Vault opened for player: " + player.getName().getString());
                                    return;
                                }
                            }
                        }
                    }
                });
    }

    private boolean isOminousVaultWithHeavyCore(VaultBlockEntity vaultEntity, ServerWorld world) {
        var config = vaultEntity.getConfig();

        if (!config.ominous()) {
            return false;
        }
        
        // Заглушка: предполагаем, что проверка показала наличие Heavy Core.
        // Рекомендуется доработать реальную проверку по таблице лута.
        return true;
    }
}
