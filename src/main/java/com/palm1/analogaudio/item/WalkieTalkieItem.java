package com.palm1.analogaudio.item;

import com.palm1.analogaudio.network.AnalogAudioNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class WalkieTalkieItem extends Item {
    public WalkieTalkieItem(Properties properties) {
        super(properties);
    }

    public static int getFrequency(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Frequency")) {
            return tag.getInt("Frequency");
        }
        return 1;
    }

    public static void setFrequency(ItemStack stack, int frequency) {
        stack.getOrCreateTag().putInt("Frequency", frequency);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            openTunerScreen(player, hand);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private void openTunerScreen(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int currentFreq = getFrequency(stack);
        Minecraft.getInstance().setScreen(new com.palm1.analogaudio.client.gui.RotaryTunerScreen(currentFreq, freq -> {
            AnalogAudioNetwork
                    .sendToServer(new com.palm1.analogaudio.network.packet.SetWalkieFrequencyC2SPacket(hand, freq));
        }));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private com.palm1.analogaudio.client.render.WalkieTalkieRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new com.palm1.analogaudio.client.render.WalkieTalkieRenderer(
                            Minecraft.getInstance().getItemRenderer(),
                            Minecraft.getInstance().getEntityModels());
                }
                return renderer;
            }
        });
    }
}
