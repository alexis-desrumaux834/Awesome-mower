package com.alexis.awesomeMower.src;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.core.io.FileSystemResource;

import com.alexis.awesomeMower.entities.LawnMower;

public class ResultFileWriter extends FlatFileItemWriter<List<LawnMower>> {
    public ResultFileWriter(FileSystemResource resource) {
        // Initialize default configuration of the writer
        this.setResource(resource);
        setLineAggregator(items -> String.join("\n",
                items.stream().map(item -> "%d %d %c".formatted(item.getX(), item.getY(), item.getDirection()))
                        .collect(Collectors.toList())));
    }
}