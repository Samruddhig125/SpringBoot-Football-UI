package com.example.UIFootball.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MatchService {
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);
    private List<Map<String, Object>> matchData = Collections.emptyList();

    public MatchService() { loadMatchData(); }

    private void loadMatchData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("all_matches.json")) {
            if (inputStream != null) {
                matchData = objectMapper.readValue(inputStream, new TypeReference<>() {});
            }
        } catch (IOException e) {
            logger.error("Error loading match data", e);
        }
    }

    public List<Map<String, Object>> getMatchData() { return matchData; }
    
    public List<String> getUniqueSeasons() {
        return matchData.stream()
                .map(match -> String.valueOf(match.get("season")))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getUniqueLeagues() {
        return matchData.stream()
                .map(match -> String.valueOf(match.get("league")))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
