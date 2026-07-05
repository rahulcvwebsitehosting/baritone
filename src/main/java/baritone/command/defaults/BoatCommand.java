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
import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class BoatCommand extends Command {

    public BoatCommand(IBaritone baritone) {
        super(baritone, "boat");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        ((Baritone) baritone).getBoatProcess().engage();
        logDirect("Boat navigation engaged toward current goal");
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Engage boat navigation toward current goal";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The boat command engages BoatProcess, which drives a boat across large bodies",
                "of water toward the current goal. You must first set a goal (e.g. #goal).",
                "",
                "Requires a boat in your hotbar (or set boatAutoPlace=true).",
                "",
                "Usage:",
                "> boat - Drive a boat toward the current goal."
        );
    }
}