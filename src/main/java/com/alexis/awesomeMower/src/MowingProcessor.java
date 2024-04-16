package com.alexis.awesomeMower.src;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.stereotype.Component;

import com.alexis.awesomeMower.entities.Boundaries;
import com.alexis.awesomeMower.entities.LawnMower;
import com.alexis.awesomeMower.entities.MowingConfig;

@Component
public class MowingProcessor implements ItemProcessor<MowingConfig, List<LawnMower>>, ItemStream {
    @Override
    public void open(ExecutionContext context) throws ItemStreamException {
        ItemStream.super.open(context);
    }

    private void handleRotateRight(LawnMower lawnMower) {
        switch (lawnMower.getDirection()) {
            case 'N' -> lawnMower.setDirection('E');
            case 'E' -> lawnMower.setDirection('S');
            case 'S' -> lawnMower.setDirection('W');
            case 'W' -> lawnMower.setDirection('N');
            default -> {
            }
        }
    }

    private void handleRotateLeft(LawnMower lawnMower) {
        switch (lawnMower.getDirection()) {
            case 'N' -> lawnMower.setDirection('W');
            case 'E' -> lawnMower.setDirection('N');
            case 'S' -> lawnMower.setDirection('E');
            case 'W' -> lawnMower.setDirection('S');
            default -> {
            }
        }
    }

    private void handleAdvance(LawnMower lawnMower, Boundaries boundaries) {
        switch (lawnMower.getDirection()) {
            case 'N' -> {
                if (lawnMower.getY() + 1 <= boundaries.getYLimit())
                    lawnMower.setY(lawnMower.getY() + 1);
            }
            case 'E' -> {
                if (lawnMower.getX() + 1 <= boundaries.getXLimit())
                    lawnMower.setX(lawnMower.getX() + 1);
            }
            case 'S' -> {
                if (lawnMower.getY() - 1 >= 0)
                    lawnMower.setY(lawnMower.getY() - 1);
            }
            case 'W' -> {
                if (lawnMower.getX() - 1 >= 0)
                    lawnMower.setX(lawnMower.getX() - 1);
            }
            default -> {
            }
        }
    }

    private void handleMowerMove(Boundaries boundaries, LawnMower mower) {
        String instructions = mower.getInstructions();

        for (int i = 0; i != instructions.length(); i += 1) {
            switch (instructions.charAt(i)) {
                case 'A' -> handleAdvance(mower, boundaries);
                case 'D' -> handleRotateRight(mower);
                case 'G' -> handleRotateLeft(mower);
                default -> {
                }
            }
        }
    }

    @Override
    public List<LawnMower> process(MowingConfig config) throws Exception {
        if (config == null)
            return null;

        for (LawnMower lawnMower : config.getLawnMowers()) {
            this.handleMowerMove(config.getBoundaries(), lawnMower);
        }
        return config.getLawnMowers();
    }
}