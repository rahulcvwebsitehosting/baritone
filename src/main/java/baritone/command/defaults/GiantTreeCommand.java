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

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.ForBlockOptionalMeta;
import baritone.api.command.exception.CommandException;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.BlockOptionalMetaLookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GiantTreeCommand extends Command {

    public GiantTreeCommand(IBaritone baritone) {
        super(baritone, "gianttree");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        List<BlockOptionalMeta> boms = new ArrayList<>();
        while (args.hasAny()) {
            boms.add(args.getDatatypeFor(ForBlockOptionalMeta.INSTANCE));
        }
        BaritoneAPI.getProvider().getWorldScanner().repack(ctx);
        logDirect(String.format("Giant-tree mining: %s", boms.toString()));
        ((Baritone) baritone).getGiantTreeProcess().mine(new BlockOptionalMetaLookup(
                boms.toArray(new BlockOptionalMeta[0])));
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
        return "Mine a giant tree top-down (climb then descend)";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The gianttree command mines tall trees by climbing the trunk first, then mining",
                "downward while standing on the remaining logs. This avoids the falling-log pile",
                "and the block-placement that normal #mine does for tall trees.",
                "",
                "Usage:",
                "> gianttree minecraft:spruce_log - Climb and mine a tall spruce.",
                "> gianttree oak_log - Same for tall oak."
        );
    }
}