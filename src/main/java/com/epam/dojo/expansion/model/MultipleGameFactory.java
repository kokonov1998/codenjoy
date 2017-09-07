package com.epam.dojo.expansion.model;

/*-
 * #%L
 * expansion - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2016 - 2017 EPAM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.RandomDice;
import com.epam.dojo.expansion.model.levels.Level;
import com.epam.dojo.expansion.model.levels.LevelsFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by Oleksandr_Baglai on 2017-09-01.
 */
// TODO test me
public class MultipleGameFactory implements GameFactory {

    private List<Expansion> rooms = new LinkedList<>();

    private LevelsFactory singleFactory;
    private LevelsFactory multipleFactory;

    private boolean waitingOthers = false;
    private Dice dice;

    public MultipleGameFactory(Dice dice,
                               LevelsFactory singleFactory,
                               LevelsFactory multipleFactory)
    {
        this.dice = dice;
        this.singleFactory = singleFactory;
        this.multipleFactory = multipleFactory;
    }

    @Override
    @NotNull
    public Expansion get(boolean isMultiple) {
        if (isMultiple) {
            Expansion game = findFreeRandomMultiple();
            if (game == null) {
                game = createNewMultiple();
            }
            if (waitingOthers) {
                game.waitingOthers();
            }
            return game;
        } else {
            return new Expansion(singleFactory.get(),
                    new RandomDice(), Expansion.SINGLE);
        }
    }

    @Nullable
    private Expansion findFreeRandomMultiple() {
        List<Expansion> free = getFreeMultipleRooms();
        if (free.isEmpty()) {
            return null;
        }
        return free.get(dice.next(free.size()));
    }

    @NotNull
    private List<Expansion> getFreeMultipleRooms() {
        return rooms.stream()
                .filter(Expansion::isFree)
                .collect(toList());
    }

    @NotNull
    private Expansion createNewMultiple() {
        Level level = selectRandomLevelType();
        Expansion game = new Expansion(Arrays.asList(level),
                new RandomDice(), Expansion.MULTIPLE);

        rooms.add(game);
        return game;
    }

    @NotNull
    private Level selectRandomLevelType() {
        List<Level> levels = multipleFactory.get();
        return levels.get(dice.next(levels.size()));
    }

    // это опция сеттинговая, она раз на всю игру
    public void setWaitingOthers(boolean waitingOthers) {
        this.waitingOthers = waitingOthers;
    }
}
