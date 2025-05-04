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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("league-overview")
public class LeagueOverviewPage extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();
    private ComboBox<String> leagueComboBox;
    private List<Map<String, Object>> matchData;

    public LeagueOverviewPage() {
        // Create header
        HorizontalLayout header = createHeader();

        // Page subtitle
        Paragraph subtitle = new Paragraph("Select a league to view detailed match data.");
        subtitle.getStyle()
                .set("color", "#666666") // Medium gray text color
                .set("text-align", "center")
                .set("margin", "10px 0");

        // Fetch match data
        MatchService matchService = new MatchService();
        matchData = matchService.getMatchData();

        // Create league filter and configure grid
        createLeagueFilter(matchService);
        configureGrid();

        // Navigation back to HomeView
        Button homeButton = new Button("Back to Home");
        homeButton.addClickListener(e -> homeButton.getUI().ifPresent(ui -> ui.navigate("")));
        styleButton(homeButton);

        // Create footer
        HorizontalLayout footer = createFooter();

        // Add components to layout
        add(header, subtitle, leagueComboBox, grid, homeButton, footer);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);
        getStyle()
            .set("background-color", "#f4f4f4") // Light gray background
            .set("padding", "20px");
    }

    private HorizontalLayout createHeader() {
        // Logo
        Image logo = new Image("https://logodix.com/logo/1943855.png", "Football Logo");
        logo.setWidth("50px");
        logo.setHeight("50px");

        // Title
        H1 title = new H1("League Overview");
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

    private void createLeagueFilter(MatchService matchService) {
        leagueComboBox = new ComboBox<>("Select League");
        leagueComboBox.setItems(matchService.getUniqueLeagues());
        leagueComboBox.setPlaceholder("Choose a league...");
        leagueComboBox.addValueChangeListener(e -> updateGrid(e.getValue()));

        // Style the dropdown
        leagueComboBox.getStyle()
            .set("background-color", "#ffffff")
            .set("color", "#333333")
            .set("border-radius", "5px")
            .set("padding", "10px")
            .set("width", "300px")
            .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
    }

    private void updateGrid(String selectedLeague) {
        if (selectedLeague == null || selectedLeague.isEmpty()) {
            grid.setItems(matchData); // Show all data if no league is selected
        } else {
            List<Map<String, Object>> filteredData = matchData.stream()
                .filter(match -> selectedLeague.equals(match.get("league")))
                .collect(Collectors.toList());
            grid.setItems(filteredData); // Show only matches for the selected league
        }
    }

    private void configureGrid() {
        grid.addColumn(match -> match.get("id")).setHeader("Match ID").setAutoWidth(true);
        grid.addColumn(match -> match.get("league")).setHeader("League").setAutoWidth(true);
        grid.addColumn(match -> match.get("season")).setHeader("Season").setAutoWidth(true);
        grid.addColumn(match -> match.get("datetime")).setHeader("Date & Time").setAutoWidth(true);

        grid.addColumn(match -> parseNestedField(match.get("h"), "title")).setHeader("Home Team").setAutoWidth(true);
        grid.addColumn(match -> parseNestedField(match.get("a"), "title")).setHeader("Away Team").setAutoWidth(true);

        grid.addColumn(match -> parseNestedField(match.get("goals"), "h") + " - " +
                             parseNestedField(match.get("goals"), "a")).setHeader("Score").setAutoWidth(true);

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
                Map<String, String> nestedMap = new ObjectMapper().readValue(validJson, Map.class);
                return nestedMap.getOrDefault(key, "N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
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