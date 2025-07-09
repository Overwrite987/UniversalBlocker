package ru.overwrite.ublocker.blockgroups;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public class GroupUtils {

    public ImmutableSet<String> createStringSet(List<String> input) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builderWithExpectedSize(input.size());
        for (String s : input) {
            builder.add(s.toLowerCase());
        }
        return builder.build();
    }

    public ImmutableSet<Pattern> createPatternSet(List<String> input) {
        ImmutableSet.Builder<Pattern> builder = ImmutableSet.builderWithExpectedSize(input.size());
        for (String s : input) {
            builder.add(Pattern.compile(s));
        }
        return builder.build();
    }

    public ImmutableList<String> createStringList(List<String> input) {
        ImmutableList.Builder<String> builder = ImmutableList.builderWithExpectedSize(input.size());
        for (String s : input) {
            builder.add(s.toLowerCase());
        }
        return builder.build();
    }

    public ImmutableList<Pattern> createPatternList(List<String> input) {
        ImmutableList.Builder<Pattern> builder = ImmutableList.builderWithExpectedSize(input.size());
        for (String s : input) {
            builder.add(Pattern.compile(s));
        }
        return builder.build();
    }
}
