package com.alexis.awesomeMower.src;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;

import com.alexis.awesomeMower.entities.Boundaries;
import com.alexis.awesomeMower.entities.LawnMower;
import com.alexis.awesomeMower.entities.MowingConfig;

import lombok.Setter;

@Component
public class ConfigFileReader implements ItemReader<MowingConfig>, ItemStream {
    @Setter
    private FlatFileItemReader<FieldSet> reader;
    
    private ExecutionContext context;
    private boolean endRead;

    public ConfigFileReader(Resource resource) {
        FlatFileItemReader<FieldSet> reader = new FlatFileItemReaderBuilder<FieldSet>().name("FieldSetItemReader")
                .lineTokenizer(new DelimitedLineTokenizer(" ")) // Get tokens separated by spaces
                .fieldSetMapper(new PassThroughFieldSetMapper()) // No mapping
                .resource(resource) // Read from given file
                .build();
        this.setReader(reader);
        this.endRead = false;
    }

    @Override
    public void open(ExecutionContext context) throws ItemStreamException {
        reader.open(context);
        this.context = context;
    }

    private boolean checkIfLineIsLMInstructions(FieldSet line) {
        if (line.getFieldCount() != 1) {
            return false;
        }

        String regex = "^[DGA]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line.readString(0));
        return matcher.matches();
    }

    private boolean checkIfLineIsLMPosition(FieldSet line) {
        if (line.getFieldCount() != 3) {
            return false;
        }
        try {
            Integer.parseInt(line.readString(0));
            Integer.parseInt(line.readString(1));
        } catch (Exception e) {
            return false;
        }
        String direction = line.readString(2);
        String regex = "^[NEWS]$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(direction);
        return matcher.matches();
    }

    private boolean mustBeBoundaries(FieldSet line) {
        if (line.getFieldCount() != 2) {
            return false;
        }
        try {
            Integer.parseInt(line.readString(0));
            Integer.parseInt(line.readString(1));
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private List<LawnMower> getLawnMowers(List<FieldSet> fileLines) {
        List<LawnMower> foundLawnMowers = new ArrayList<LawnMower>();
        int flag = 0;
        LawnMower lastCreatedMower = new LawnMower();

        for (int i = 1; i != fileLines.size(); i += 1) {
            if (checkIfLineIsLMPosition(fileLines.get(i)) == true) {
                lastCreatedMower = new LawnMower();
                lastCreatedMower.setX(fileLines.get(i).readInt(0));
                lastCreatedMower.setY(fileLines.get(i).readInt(1));
                lastCreatedMower.setDirection(fileLines.get(i).readChar(2));
                flag = 1;
            } else if (flag == 1 && checkIfLineIsLMInstructions(fileLines.get(i)) == true) {
                lastCreatedMower.setInstructions(fileLines.get(i).readString(0));
                foundLawnMowers.add(lastCreatedMower);
            }
        }
        return foundLawnMowers;
    }

    @Override
    public MowingConfig read() throws Exception {
        if (endRead == true)
            return null;

        List<FieldSet> fileLines = new ArrayList<FieldSet>();
        FieldSet line;

        reader.open(context);

        while ((line = reader.read()) != null) {
            fileLines.add(line);
        }
        if (fileLines.isEmpty()) {
            throw new Exception("Input file shouldn't be empty");
        }
        if (mustBeBoundaries(fileLines.get(0)) == false) {
            throw new Exception("No valid boundaries found in given input file");
        }
        Boundaries boundaries = new Boundaries();
        boundaries.setXLimit(fileLines.get(0).readInt(0));
        boundaries.setYLimit(fileLines.get(0).readInt(1));
        List<LawnMower> lawnMowers = this.getLawnMowers(fileLines);
        MowingConfig mowingConfig = new MowingConfig();
        mowingConfig.setBoundaries(boundaries);
        mowingConfig.setLawnMowers(lawnMowers);
        this.endRead = true;
        return mowingConfig;
    }
}
