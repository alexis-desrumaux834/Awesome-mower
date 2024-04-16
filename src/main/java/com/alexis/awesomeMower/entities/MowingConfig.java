package com.alexis.awesomeMower.entities;

import java.util.List;

import lombok.Data;

@Data
public class MowingConfig {
  private Boundaries boundaries;
  private List<LawnMower> lawnMowers;
}
