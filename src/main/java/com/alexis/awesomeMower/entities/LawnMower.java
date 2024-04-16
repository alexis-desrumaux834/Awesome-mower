package com.alexis.awesomeMower.entities;
import lombok.Data;

@Data
public class LawnMower {
    private int x;
    private int y;
    private char direction;
    private String instructions;
}
