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
import baritone.api.utils.BlockOptionalMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class FarmListCommand extends Command {

    public FarmListCommand(IBaritone baritone) {
        super(baritone, "farmlist");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        List<BlockOptionalMeta> boms = new ArrayList<>();
        while (args.hasAny()) {
            boms.add(args.getDatatypeFor(ForBlockOptionalMeta.INSTANCE));
        }
        BaritoneAPI.getProvider().getWorldScanner().repack(ctx);
        logDirect(String.format("Farming %s", boms.toString()));
        baritone.getFarmProcess().farm(0, null, boms.toArray(new BlockOptionalMeta[0]));
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
                "> farmlist wheat carrots potato - Only farms wheat, carrots, and potatoes.",
                "> farmlist - Lists all available crop types that can be farmed (not yet implemented)."
        );
    }
}