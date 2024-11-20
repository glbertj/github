package com.svx.github.view;

import com.svx.github.model.Repository;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import java.util.Objects;

public class MainLayoutView extends View<BorderPane> {
    // Top Bar Components
    private final VBox topBarContainer = new VBox();
    private ComboBox<Repository> repositoryDropdown;
    private Button multiFunctionButton;

    // Menu
    private MenuItem createRepositoryMenu;
    private MenuItem addRepositoryMenu;
    private MenuItem cloneRepositoryMenu;
    private MenuItem logoutMenu;
    private MenuItem exitMenu;

    // Sidebar Components
    private final BorderPane sideBar = new BorderPane();
    private Button changesButton;
    private Button historyButton;
    private VBox changesTab;
    private VBox changedFilesList;
    private Button commitButton;
    private VBox historyTab;
    private VBox historyList;

    // Main Content
    private final StackPane mainContent = new StackPane();
    private TextArea textArea;

    @Override
    public void initializeView() {
        root = new BorderPane();

        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/github/style/main-layout.css")
        ).toExternalForm();

        initializeTopBar();
        initializeSideBar();
        initializeMainContent();

        root.setTop(topBarContainer);
        root.setLeft(sideBar);
        root.setCenter(mainContent);
    }

    private void initializeTopBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        createRepositoryMenu = new MenuItem("New Repository...");
        addRepositoryMenu = new MenuItem("Add Local Repository...");
        cloneRepositoryMenu = new MenuItem("Clone Repository...");
        logoutMenu = new MenuItem("Logout");
        exitMenu = new MenuItem("Exit");
        fileMenu.getItems().addAll(
                createRepositoryMenu, new SeparatorMenuItem(),
                addRepositoryMenu, new SeparatorMenuItem(),
                cloneRepositoryMenu, new SeparatorMenuItem(),
                logoutMenu, exitMenu
        );

        menuBar.getMenus().add(fileMenu);

        repositoryDropdown = createRepositoryDropdown();
        multiFunctionButton = createAnimatedButton("Push Origin");

        HBox topBar = new HBox(repositoryDropdown, multiFunctionButton);
        topBar.setSpacing(10);
        topBar.getStyleClass().add("top-bar-container");

        topBarContainer.getChildren().addAll(menuBar, topBar);
    }

    private static ComboBox<Repository> createRepositoryDropdown() {
        ComboBox<Repository> dropdown = new ComboBox<>(Repository.getRepositories());
        dropdown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Repository repository) {
                return repository != null ? repository.getName() : "";
            }

            @Override
            public Repository fromString(String string) {
                return null;
            }
        });
        dropdown.setPromptText("Select a Repository");
        dropdown.setPrefWidth(200);
        dropdown.getStyleClass().add("combo-box");
        return dropdown;
    }

    private void initializeSideBar() {
        changesButton = createSidebarButton("Changes");
        changesButton.getStyleClass().add("active");
        historyButton = createSidebarButton("History");

        HBox sideBarHeader = new HBox(changesButton, historyButton);
        sideBarHeader.setSpacing(5);
        sideBarHeader.getStyleClass().add("sidebar-header");

        sideBar.setTop(sideBarHeader);

        initializeChangesTab();
        initializeHistoryTab();

        showChangesTab();
    }

    private Button createSidebarButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button");
        button.setOnAction(e -> handleTabSwitch(button));
        return button;
    }

    private void handleTabSwitch(Button activeButton) {
        changesButton.getStyleClass().remove("active");
        historyButton.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");

        if (activeButton == changesButton) {
            showChangesTab();
        } else if (activeButton == historyButton) {
            showHistoryTab();
        }
    }

    private void initializeChangesTab() {
        changesTab = new VBox();
        changesTab.getStyleClass().add("tab-content");

        Label changesLabel = new Label("Changes");
        changesLabel.getStyleClass().add("tab-label");

        changedFilesList = new VBox();
        changedFilesList.setSpacing(5);

        commitButton = createAnimatedButton("Commit");
        commitButton.getStyleClass().add("commit-button");

        changesTab.getChildren().addAll(changesLabel, changedFilesList, commitButton);
    }


    private void initializeHistoryTab() {
        historyTab = new VBox();
        historyTab.getStyleClass().add("tab-content");

        Label historyLabel = new Label("History");
        historyLabel.getStyleClass().add("tab-label");

        historyList = new VBox();

        historyTab.getChildren().addAll(historyLabel, historyList);
    }

    private void initializeMainContent() {
        textArea = new TextArea();
        textArea.getStyleClass().add("text-area");
        textArea.setWrapText(true);

        mainContent.getChildren().add(textArea);
    }

    public void showChangesTab() {
        sideBar.setCenter(changesTab);
    }

    public void showHistoryTab() {
        sideBar.setCenter(historyTab);
    }

    public MenuItem getCreateRepositoryMenu() { return createRepositoryMenu; }
    public MenuItem getAddRepositoryMenu() { return addRepositoryMenu; }
    public MenuItem getCloneRepositoryMenu() { return cloneRepositoryMenu; }
    public MenuItem getLogoutMenu() { return logoutMenu; }
    public MenuItem getExitMenu() { return exitMenu; }
    public ComboBox<Repository> getRepositoryDropdown() { return repositoryDropdown; }
    public Button getMultiFunctionButton() { return multiFunctionButton; }
    public Button getChangesButton() { return changesButton; }
    public Button getHistoryButton() { return historyButton; }
    public Button getCommitButton() { return commitButton; }
    public VBox getChangedFilesList() { return changedFilesList; }
    public VBox getHistoryList() { return historyList; }
    public TextArea getTextArea() { return textArea; }
}