/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.command.defaults;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.ForBlockOptionalMeta;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.utils.BlockOptionalMeta;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FarmListCommand extends Command {

    private static final Set<Block> FARMABLE_BLOCKS = new HashSet<>(Arrays.asList(
            Blocks.WHEAT,
            Blocks.CARROTS,
            Blocks.POTATOES,
            Blocks.BEETROOTS,
            Blocks.TORCHFLOWER_CROP,
            Blocks.TORCHFLOWER,
            Blocks.PITCHER_CROP,
            Blocks.PUMPKIN,
            Blocks.MELON,
            Blocks.NETHER_WART,
            Blocks.COCOA,
            Blocks.SWEET_BERRY_BUSH,
            Blocks.SUGAR_CANE,
            Blocks.BAMBOO,
            Blocks.CACTUS
    ));

    public FarmListCommand(IBaritone baritone) {
        super(baritone, "farmlist");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        Set<BlockOptionalMeta> boms = new LinkedHashSet<>();
        while (args.hasAny()) {
            BlockOptionalMeta bom = args.getDatatypeFor(ForBlockOptionalMeta.INSTANCE);
            Block block = bom.getBlock();
            if (!FARMABLE_BLOCKS.contains(block)) {
                throw new CommandInvalidTypeException(
                        args.consumed(),
                        "a farmable crop block",
                        block.getDescriptionId()
                );
            }
            boms.add(bom);
        }
        BaritoneAPI.getProvider().getWorldScanner().repack(ctx);
        String cropList = boms.stream()
                .map(bom -> {
                    String name = bom.getBlock().getDescriptionId();
                    return name.substring(name.lastIndexOf('.') + 1);
                })
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(", "));
        logDirect(String.format("Now farming: %s", cropList));
        baritone.getFarmProcess().farm(boms.toArray(new BlockOptionalMeta[0]));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        while (args.has(2)) {
            args.getDatatypeFor(ForBlockOptionalMeta.INSTANCE);
        }
        return args.tabCompleteDatatype(ForBlockOptionalMeta.INSTANCE);
    }

    @Override
    public String getShortDesc() {
        return "Farm specific crops";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The farmlist command allows you to tell Baritone to farm only specific crops.",
                "",
                "By specifying crop names, only those crops will be harvested and replanted.",
                "All other crops will be ignored.",
                "",
                "Usage:",
                "> farmlist minecraft:wheat minecraft:carrots - Only farms wheat and carrots.",
                "> farmlist wheat carrots potato - Only farms wheat, carrots, and potatoes."
        );
    }
}