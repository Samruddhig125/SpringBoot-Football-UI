package com.example.UIFootball.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.example.UIFootball.service.MatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Route("")
public class HomeView extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Map<String, Object>> matchData;

    private ComboBox<String> yearCombo;
    private ComboBox<String> leagueCombo;
    private TextField teamField;
    private TextField matchIdField;

    public HomeView() {
        // Apply background color
        getStyle()
            .set("background-color", "#f4f4f4")
            .set("padding", "0");

        // Create header
        HorizontalLayout header = createHeader();

        // Create navigation bar
        HorizontalLayout navigationBar = createNavigationBar();

        // Fetch match data
        MatchService matchService = new MatchService();
        matchData = matchService.getMatchData();

        // Create filters and grid
        createFilterComponents(matchService);
        configureGrid();

        // Add CRUD operations section
        HorizontalLayout crudOperations = createCrudOperations();

        // Create footer
        HorizontalLayout footer = createFooter();

        // Add components to the layout
        add(header, navigationBar, createFiltersLayout(), grid, crudOperations, footer);
    }

    private HorizontalLayout createHeader() {
        // Logo
        Image logo = new Image("https://logodix.com/logo/1943855.png", "Football Logo");
        logo.setWidth("50px");
        logo.setHeight("50px");
    
        // Title
        H1 title = new H1("Football Analysis Website");
        title.getStyle()
             .set("color", "#ffffff")
             .set("font-size", "36px") // Larger font size
             .set("font-weight", "bold") // Bold font
             .set("margin", "0")
             .set("text-align", "center") // Center the title
             .set("flex-grow", "1"); // Ensure it takes up space to center
    
        // Header layout
        HorizontalLayout header = new HorizontalLayout(logo, title);
        header.setAlignItems(Alignment.CENTER);
        header.setWidthFull();
        header.getStyle()
              .set("background-color", "#4caf50")
              .set("padding", "10px 20px")
              .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)");
    
        return header;
    }

    private HorizontalLayout createNavigationBar() {
        Button homeButton = new Button("Home");
        homeButton.addClickListener(e -> homeButton.getUI().ifPresent(ui -> ui.navigate("")));

        Button seasonSummaryButton = new Button("Season Summary");
        seasonSummaryButton.addClickListener(e -> seasonSummaryButton.getUI().ifPresent(ui -> ui.navigate("season-summary")));

        Button leagueOverviewButton = new Button("League Overview");
        leagueOverviewButton.addClickListener(e -> leagueOverviewButton.getUI().ifPresent(ui -> ui.navigate("league-overview")));

        styleNavButton(homeButton);
        styleNavButton(seasonSummaryButton);
        styleNavButton(leagueOverviewButton);

        HorizontalLayout navigationBar = new HorizontalLayout(homeButton, seasonSummaryButton, leagueOverviewButton);
        navigationBar.setWidthFull();
        //navigationBar.setJustifyContentMode(JustifyContentMode.CENTER);
        navigationBar.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        navigationBar.getStyle()
                     .set("background-color", "#4caf50")
                     .set("padding", "10px 0")
                     .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        return navigationBar;
    }

    private HorizontalLayout createCrudOperations() {
        Button addButton = new Button("Add Match");
        Button editButton = new Button("Edit Match");
        Button deleteButton = new Button("Delete Match");
    
        styleButton(addButton);
        styleButton(editButton);
        styleButton(deleteButton);
    
        // Add Match Dialog
        addButton.addClickListener(e -> {
            Dialog addDialog = new Dialog();
            VerticalLayout dialogLayout = new VerticalLayout();
        
            // Style the dialog
            addDialog.getElement().getStyle()
                     .set("background-color", "#4caf50") // Green background
                     .set("color", "#ffffff") // White text
                     .set("border-radius", "8px") // Rounded corners
                     .set("padding", "20px"); // Padding inside the dialog
        
            TextField seasonField = new TextField("Season");
            seasonField.setPlaceholder("Enter season (e.g., 2025)");
        
            TextField leagueField = new TextField("League");
            leagueField.setPlaceholder("Enter league (e.g., EPL)");
        
            TextField homeTeamField = new TextField("Home Team");
            homeTeamField.setPlaceholder("Enter home team");
        
            TextField awayTeamField = new TextField("Away Team");
            awayTeamField.setPlaceholder("Enter away team");
        
            TextField goalsField = new TextField("Goals");
            goalsField.setPlaceholder("Enter goals (e.g., 2-1)");
        
            Button saveButton = new Button("Save", saveEvent -> {
                Map<String, Object> newMatch = new HashMap<>();
                newMatch.put("id", matchData.size() + 1); // Auto-generate ID
                newMatch.put("season", seasonField.getValue());
                newMatch.put("league", leagueField.getValue());
                newMatch.put("h", "{'title':'" + homeTeamField.getValue() + "'}");
                newMatch.put("a", "{'title':'" + awayTeamField.getValue() + "'}");
                newMatch.put("goals", "{'h':'" + goalsField.getValue().split("-")[0] + "','a':'" + goalsField.getValue().split("-")[1] + "'}");
                matchData.add(newMatch);
                grid.setItems(matchData); // Refresh grid
                addDialog.close();
                System.out.println("Match added: " + newMatch);
            });
        
            Button cancelButton = new Button("Cancel", cancelEvent -> addDialog.close());
        
            // Style buttons
            styleButton(saveButton);
            styleButton(cancelButton);
        
            dialogLayout.add(seasonField, leagueField, homeTeamField, awayTeamField, goalsField, saveButton, cancelButton);
            addDialog.add(dialogLayout);
            addDialog.open();
        });
    
        // Edit Match Dialog
        editButton.addClickListener(e -> {
            Map<String, Object> selectedMatch = grid.asSingleSelect().getValue();
            if (selectedMatch != null) {
                Dialog editDialog = new Dialog();
                VerticalLayout dialogLayout = new VerticalLayout();
    
                TextField homeTeamField = new TextField("Home Team");
                homeTeamField.setValue(parseNestedField(selectedMatch.get("h"), "title"));
    
                TextField awayTeamField = new TextField("Away Team");
                awayTeamField.setValue(parseNestedField(selectedMatch.get("a"), "title"));
    
                TextField goalsField = new TextField("Goals");
                goalsField.setValue(parseNestedField(selectedMatch.get("goals"), "h") + "-" + parseNestedField(selectedMatch.get("goals"), "a"));
    
                Button saveButton = new Button("Save", saveEvent -> {
                    selectedMatch.put("h", "{'title':'" + homeTeamField.getValue() + "'}");
                    selectedMatch.put("a", "{'title':'" + awayTeamField.getValue() + "'}");
                    selectedMatch.put("goals", "{'h':'" + goalsField.getValue().split("-")[0] + "','a':'" + goalsField.getValue().split("-")[1] + "'}");
                    grid.getDataProvider().refreshItem(selectedMatch); // Refresh grid
                    editDialog.close();
                    System.out.println("Match edited: " + selectedMatch);
                });
    
                Button cancelButton = new Button("Cancel", cancelEvent -> editDialog.close());
    
                dialogLayout.add(homeTeamField, awayTeamField, goalsField, saveButton, cancelButton);
                editDialog.add(dialogLayout);
                editDialog.open();
            } else {
                Notification.show("No match selected for editing.");
            }
        });
    
        // Delete Match Dialog
        deleteButton.addClickListener(e -> {
            Map<String, Object> selectedMatch = grid.asSingleSelect().getValue();
            if (selectedMatch != null) {
                Dialog deleteDialog = new Dialog();
                VerticalLayout dialogLayout = new VerticalLayout();
    
                Label confirmationLabel = new Label("Are you sure you want to delete this match?");
                Button confirmButton = new Button("Delete", confirmEvent -> {
                    matchData.remove(selectedMatch);
                    grid.setItems(matchData); // Refresh grid
                    deleteDialog.close();
                    System.out.println("Match deleted: " + selectedMatch);
                });
    
                Button cancelButton = new Button("Cancel", cancelEvent -> deleteDialog.close());
    
                dialogLayout.add(confirmationLabel, confirmButton, cancelButton);
                deleteDialog.add(dialogLayout);
                deleteDialog.open();
            } else {
                Notification.show("No match selected for deletion.");
            }
        });
    
        HorizontalLayout crudOperations = new HorizontalLayout(addButton, editButton, deleteButton);
        crudOperations.setWidthFull();
        crudOperations.setJustifyContentMode(JustifyContentMode.CENTER);
        crudOperations.setSpacing(true);
        crudOperations.getStyle()
                      .set("margin-top", "20px");
    
        return crudOperations;
    }

        

    private HorizontalLayout createFooter() {
        Paragraph footerText = new Paragraph("© 2025 Football Analysis Website. All rights reserved.");
        footerText.getStyle()
                  .set("color", "#ffffff")
                  .set("font-size", "14px")
                  .set("margin", "0");

        HorizontalLayout footer = new HorizontalLayout(footerText);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.CENTER);
        footer.getStyle()
              .set("background-color", "#4caf50")
              .set("padding", "10px 0")
              .set("margin-top", "20px");

        return footer;
    }

    private void styleNavButton(Button button) {
        button.getStyle()
              .set("background-color", "#ffffff")
              .set("color", "#4caf50")
              .set("border", "1px solid #4caf50")
              .set("border-radius", "5px")
              .set("padding", "10px 20px")
              .set("cursor", "pointer")
              .set("margin", "5px");
    }

    private void styleButton(Button button) {
        button.getStyle()
              .set("background-color", "#4caf50")
              .set("color", "#ffffff")
              .set("border-radius", "5px")
              .set("padding", "10px 20px")
              .set("cursor", "pointer")
              .set("margin", "5px");
    }

    private void styleDropdown(ComboBox<?> dropdown) {
        dropdown.getStyle()
                .set("--lumo-primary-text-color", "#333333")
                .set("--lumo-primary-color", "#ffffff")
                .set("--lumo-border-radius", "5px");
    }

    private void styleTextField(TextField textField) {
        textField.getStyle()
                 .set("--lumo-primary-text-color", "#333333")
                 .set("--lumo-base-color", "#ffffff")
                 .set("--lumo-border-radius", "5px");
    }

    private void configureGrid() {
        grid.setItems(matchData);

        grid.addColumn(match -> match.get("id")).setHeader("Match ID").setAutoWidth(true);
        grid.addColumn(match -> parseNestedField(match.get("h"), "title")).setHeader("Home Team").setAutoWidth(true);
        grid.addColumn(match -> parseNestedField(match.get("a"), "title")).setHeader("Away Team").setAutoWidth(true);
        grid.addColumn(match -> parseNestedField(match.get("goals"), "h") + " - " +
                              parseNestedField(match.get("goals"), "a")).setHeader("Score").setAutoWidth(true);
        grid.addColumn(match -> String.valueOf(match.get("season"))).setHeader("Season").setAutoWidth(true);

        grid.setWidthFull();
        grid.getStyle()
            .set("--lumo-primary-color", "#ffffff")
            .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)");
    }

    private String parseNestedField(Object field, String key) {
        try {
            if (field instanceof String) {
                String validJson = ((String) field).replace("'", "\"");
                Map<String, String> nestedMap = objectMapper.readValue(validJson, Map.class);
                return nestedMap.getOrDefault(key, "N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void createFilterComponents(MatchService matchService) {
        yearCombo = new ComboBox<>("Season");
        yearCombo.setItems(matchService.getUniqueSeasons());
        yearCombo.setPlaceholder("All Years");
        styleDropdown(yearCombo);

        leagueCombo = new ComboBox<>("League");
        leagueCombo.setItems(matchService.getUniqueLeagues());
        leagueCombo.setPlaceholder("All Leagues");
        styleDropdown(leagueCombo);

        teamField = new TextField("Team");
        teamField.setPlaceholder("Search teams...");
        teamField.setValueChangeMode(ValueChangeMode.EAGER);
        styleTextField(teamField);

        matchIdField = new TextField("Match ID");
        matchIdField.setPlaceholder("Search by Match ID...");
        matchIdField.setValueChangeMode(ValueChangeMode.EAGER);
        styleTextField(matchIdField);

        yearCombo.addValueChangeListener(e -> updateFilters());
        leagueCombo.addValueChangeListener(e -> updateFilters());
        teamField.addValueChangeListener(e -> updateFilters());
        matchIdField.addValueChangeListener(e -> updateFilters());
    }

    private HorizontalLayout createFiltersLayout() {
        HorizontalLayout filters = new HorizontalLayout(yearCombo, leagueCombo, teamField, matchIdField);
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.getStyle()
               .set("background-color", "#ffffff")
               .set("padding", "10px")
               .set("border-radius", "5px")
               .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        yearCombo.setWidth("200px");
        leagueCombo.setWidth("200px");
        teamField.setWidth("300px");
        matchIdField.setWidth("200px");

        return filters;
    }

    private void updateFilters() {
        String selectedYear = yearCombo.getValue();
        String selectedLeague = leagueCombo.getValue();
        String teamSearch = teamField.getValue();
        String matchIdSearch = matchIdField.getValue();

        List<Map<String, Object>> filteredData = matchData.stream()
            .filter(match -> {
                boolean matchesYear = (selectedYear == null || String.valueOf(match.get("season")).equals(selectedYear));
                boolean matchesLeague = (selectedLeague == null || String.valueOf(match.get("league")).equalsIgnoreCase(selectedLeague));
                boolean matchesTeam = (teamSearch == null || teamSearch.isEmpty() ||
                    parseNestedField(match.get("h"), "title").toLowerCase().contains(teamSearch.toLowerCase()) ||
                    parseNestedField(match.get("a"), "title").toLowerCase().contains(teamSearch.toLowerCase()));
                boolean matchesMatchId = (matchIdSearch == null || matchIdSearch.isEmpty() ||
                    String.valueOf(match.get("id")).contains(matchIdSearch));

                return matchesYear && matchesLeague && matchesTeam && matchesMatchId;
            })
            .collect(Collectors.toList());

        grid.setItems(filteredData);
    }
}