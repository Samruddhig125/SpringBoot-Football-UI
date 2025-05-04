package com.example.UIFootball.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.example.UIFootball.service.MatchService;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import java.util.*;
import java.util.stream.Collectors;

@Route("season-summary")
public class SeasonSummaryPage extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();
    private ComboBox<String> leagueComboBox;
    private ComboBox<String> teamComboBox;
    private List<Map<String, Object>> matchData;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SeasonSummaryPage() {
        // Create header
        HorizontalLayout header = createHeader();

        // Page subtitle
        Paragraph subtitle = new Paragraph("View season performance summary for teams.");
        subtitle.getStyle()
                .set("color", "#666666") // Medium gray text color
                .set("text-align", "center")
                .set("margin", "10px 0");

        // Fetch match data
        MatchService matchService = new MatchService();
        matchData = matchService.getMatchData();

        // Create filters and configure grid
        createFilters(matchService);
        configureGrid();

        // Navigation back to HomeView
        Button homeButton = new Button("Back to Home");
        homeButton.addClickListener(e -> homeButton.getUI().ifPresent(ui -> ui.navigate("")));
        styleButton(homeButton);

        // Create footer
        HorizontalLayout footer = createFooter();

        // Add components to layout
        add(header, subtitle, createFiltersLayout(), grid, homeButton, footer);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);
        getStyle()
            .set("background-color", "#f4f4f4") // Light gray background
            .set("padding", "20px");
    }

    private List<Map<String, Object>> generateSeasonSummary(String selectedLeague, String selectedTeam) {
        List<Map<String, Object>> summaryData = new ArrayList<>();
    
        // Filter matches based on the selected league and team
        List<Map<String, Object>> filteredMatches = matchData.stream()
            .filter(match -> (selectedLeague == null || selectedLeague.equals(match.get("league"))) &&
                             (selectedTeam == null || selectedTeam.equals(parseNestedField(match.get("h"), "title")) ||
                              selectedTeam.equals(parseNestedField(match.get("a"), "title"))))
            .collect(Collectors.toList());
    
        // Group matches by team
        Map<String, List<Map<String, Object>>> matchesByTeam = new HashMap<>();
        for (Map<String, Object> match : filteredMatches) {
            String homeTeam = parseNestedField(match.get("h"), "title");
            String awayTeam = parseNestedField(match.get("a"), "title");
    
            matchesByTeam.putIfAbsent(homeTeam, new ArrayList<>());
            matchesByTeam.putIfAbsent(awayTeam, new ArrayList<>());
    
            matchesByTeam.get(homeTeam).add(match);
            matchesByTeam.get(awayTeam).add(match);
        }
    
        // Calculate summary for each team
        for (Map.Entry<String, List<Map<String, Object>>> entry : matchesByTeam.entrySet()) {
            String team = entry.getKey();
            List<Map<String, Object>> teamMatches = entry.getValue();
    
            int played = teamMatches.size();
            int wins = 0, draws = 0, losses = 0, goalsFor = 0, goalsAgainst = 0;
    
            for (Map<String, Object> match : teamMatches) {
                String homeTeam = parseNestedField(match.get("h"), "title");
                String awayTeam = parseNestedField(match.get("a"), "title");
    
                int homeGoals = Integer.parseInt(parseNestedField(match.get("goals"), "h"));
                int awayGoals = Integer.parseInt(parseNestedField(match.get("goals"), "a"));
    
                if (team.equals(homeTeam)) {
                    goalsFor += homeGoals;
                    goalsAgainst += awayGoals;
                    if (homeGoals > awayGoals) wins++;
                    else if (homeGoals == awayGoals) draws++;
                    else losses++;
                } else if (team.equals(awayTeam)) {
                    goalsFor += awayGoals;
                    goalsAgainst += homeGoals;
                    if (awayGoals > homeGoals) wins++;
                    else if (awayGoals == homeGoals) draws++;
                    else losses++;
                }
            }
    
            int points = (wins * 3) + draws;
    
            // Add summary data for the team
            Map<String, Object> teamSummary = new HashMap<>();
            teamSummary.put("team", team);
            teamSummary.put("season", "2025"); // Example season
            teamSummary.put("played", played);
            teamSummary.put("wins", wins);
            teamSummary.put("draws", draws);
            teamSummary.put("losses", losses);
            teamSummary.put("goalsFor", goalsFor);
            teamSummary.put("goalsAgainst", goalsAgainst);
            teamSummary.put("points", points);
    
            summaryData.add(teamSummary);
        }
    
        return summaryData;
    }

    private HorizontalLayout createHeader() {
        // Logo
        Image logo = new Image("https://logodix.com/logo/1943855.png", "Football Logo");
        logo.setWidth("50px");
        logo.setHeight("50px");

        // Title
        H1 title = new H1("Season Summary");
        title.getStyle()
             .set("color", "#ffffff")
             .set("font-size", "36px") // Larger font size
             .set("font-weight", "bold") // Bold font
             .set("margin", "0")
             .set("text-align", "center")
             .set("flex-grow", "1");

        // Header layout
        HorizontalLayout header = new HorizontalLayout(logo, title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle()
              .set("background-color", "#4caf50")
              .set("padding", "10px 20px")
              .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)");

        return header;
    }

    private HorizontalLayout createFooter() {
        Paragraph footerText = new Paragraph("© 2025 Football Analysis Website. All rights reserved.");
        footerText.getStyle()
                  .set("color", "#ffffff")
                  .set("font-size", "14px")
                  .set("margin", "0");

        HorizontalLayout footer = new HorizontalLayout(footerText);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footer.getStyle()
              .set("background-color", "#4caf50")
              .set("padding", "10px 0")
              .set("margin-top", "20px");

        return footer;
    }

    private HorizontalLayout createFiltersLayout() {
        HorizontalLayout filters = new HorizontalLayout(leagueComboBox, teamComboBox);
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.getStyle()
               .set("background-color", "#ffffff")
               .set("padding", "10px")
               .set("border-radius", "5px")
               .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
        return filters;
    }

    private void createFilters(MatchService matchService) {
        // League filter
        leagueComboBox = new ComboBox<>("Select League");
        leagueComboBox.setItems(matchService.getUniqueLeagues());
        leagueComboBox.setPlaceholder("All Leagues");
        leagueComboBox.addValueChangeListener(e -> updateFilters());
        styleDropdown(leagueComboBox);

        // Team filter
        Set<String> allTeams = new HashSet<>();
        matchData.forEach(match -> {
            allTeams.add(parseNestedField(match.get("h"), "title"));
            allTeams.add(parseNestedField(match.get("a"), "title"));
        });

        teamComboBox = new ComboBox<>("Select Team");
        teamComboBox.setItems(allTeams.stream().filter(team -> !team.equals("N/A")).sorted().collect(Collectors.toList()));
        teamComboBox.setPlaceholder("All Teams");
        teamComboBox.addValueChangeListener(e -> updateFilters());
        styleDropdown(teamComboBox);
    }

    private void updateFilters() {
        String selectedLeague = leagueComboBox.getValue();
        String selectedTeam = teamComboBox.getValue();

        // Generate and update season summary based on filters
        List<Map<String, Object>> summaryData = generateSeasonSummary(selectedLeague, selectedTeam);
        grid.setItems(summaryData);
    }

    private void configureGrid() {
        // Configure grid columns
        grid.addColumn(data -> data.get("team")).setHeader("Team").setAutoWidth(true);
        grid.addColumn(data -> data.get("season")).setHeader("Season").setAutoWidth(true);
        grid.addColumn(data -> data.get("played")).setHeader("Played").setAutoWidth(true);
        grid.addColumn(data -> data.get("wins")).setHeader("Wins").setAutoWidth(true);
        grid.addColumn(data -> data.get("draws")).setHeader("Draws").setAutoWidth(true);
        grid.addColumn(data -> data.get("losses")).setHeader("Losses").setAutoWidth(true);
        grid.addColumn(data -> data.get("goalsFor")).setHeader("Goals For").setAutoWidth(true);
        grid.addColumn(data -> data.get("goalsAgainst")).setHeader("Goals Against").setAutoWidth(true);
        grid.addColumn(data -> data.get("points")).setHeader("Points").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("400px");
        grid.getStyle()
            .set("background-color", "#ffffff")
            .set("color", "#333333")
            .set("border-radius", "10px")
            .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)");
    }

    private String parseNestedField(Object field, String key) {
        try {
            if (field instanceof String) {
                String validJson = ((String) field).replace("\'", "\"");
                Map<String, String> nestedMap = objectMapper.readValue(validJson, Map.class);
                return nestedMap.getOrDefault(key, "N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void styleDropdown(ComboBox<?> dropdown) {
        dropdown.getStyle()
                .set("--lumo-primary-text-color", "#333333")
                .set("--lumo-primary-color", "#ffffff")
                .set("width", "250px")
                .set("border-radius", "5px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
    }

    private void styleButton(Button button) {
        button.getStyle()
              .set("background-color", "#4caf50")
              .set("color", "white")
              .set("border-radius", "5px")
              .set("padding", "10px 20px")
              .set("margin-top", "20px");
    }
}