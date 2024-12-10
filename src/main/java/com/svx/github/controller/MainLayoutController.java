package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CloneRepositoryDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.controller.dialog.StartGameDialogController;
import com.svx.github.manager.ConnectionManager;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.manager.SessionManager;
import com.svx.github.model.*;
import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.repository.TreeRepository;
import com.svx.github.utility.*;
import com.svx.github.view.MainLayoutView;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.fxmisc.richtext.InlineCssTextArea;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainLayoutController extends Controller<MainLayoutView> {

    public MainLayoutController(AppController appController) {
        super(new MainLayoutView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        setMenuActions();
        setTopBarActions();
        setSidebarActions();
        setListeners();
        updateChangedFilesList();
        updateHistoryTab();
    }

    private void setMenuActions() {
        view.getCreateRepositoryMenu().setOnAction(e -> appController.openDialog(new CreateRepositoryDialogController(appController)));
        view.getAddRepositoryMenu().setOnAction(e -> appController.openDialog(new AddRepositoryDialogController(appController)));
        view.getCloneRepositoryMenu().setOnAction(e -> {
            if (ConnectionManager.isNotOnline()) {
                appController.openDialog(new StartGameDialogController(appController));
                return;
            }

            appController.openDialog(new CloneRepositoryDialogController(appController));
        });
        view.getLogoutMenu().setOnAction(e -> {
            appController.logout();
            appController.showNotification("Logged out.", NotificationBox.NotificationType.SUCCESS, "fas-sign-out-alt");
            if (RepositoryManager.getCurrentRepository() != null) {
                try {
                    RepositoryManager.setCurrentRepository(null);
                } catch (IOException | SQLException ex) {
                    appController.showNotification("Failed to logout.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
            }
            RepositoryManager.deleteRecentRepository();
        });
        view.getExitMenu().setOnAction(e -> appController.exitApp());

        view.getChangesMenuItem().setOnAction(e -> switchToChangesTab());
        view.getHistoryMenuItem().setOnAction(e -> switchToHistoryTab());
        view.getToggleFullScreenMenuItem().setOnAction(e -> appController.toggleFullScreen());
        view.getRemoveRepositoryMenuItem().setOnAction(e -> {
            if (RepositoryManager.getCurrentRepository() != null) {
                RepositoryManager.removeRepository();
                updateChangedFilesList();
                updateHistoryTab();
                updateMultiFunctionButton();
                appController.showNotification("Repository removed successfully.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            } else {
                appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            }
        });

        view.getShowInExplorerMenuItem().setOnAction(e -> {
            Repository currentRepo = RepositoryManager.getCurrentRepository();
            if (currentRepo != null) {
                try {
                    if (!DesktopUtility.openSystemExplorer(String.valueOf(currentRepo.getPath()))) {
                        appController.showNotification("Path does not exist..", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
                    }
                } catch (IOException ex) {
                    appController.showNotification("Failed to open system explorer.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
            } else {
                appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            }
        });

        view.getShowInVsCodeMenuItem().setOnAction(e -> {
            Repository currentRepo = RepositoryManager.getCurrentRepository();
            if (currentRepo != null) {
                try {
                    DesktopUtility.openVSCode(String.valueOf(currentRepo.getPath()));
                } catch (IOException | InterruptedException ex) {
                    appController.showNotification("Failed to open Visual Studio Code.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
            } else {
                appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            }
        });
    }

    private void setTopBarActions() {
        view.getRepositoryToggleButton().setOnMouseClicked(e -> view.switchSideBar());
        view.getOriginButton().setOnMouseClicked(e -> updateMultiFunctionButton());
    }

    private void setSidebarActions() {
        view.getChangesButton().setOnAction(e -> switchToChangesTab());
        view.getHistoryButton().setOnAction(e -> switchToHistoryTab());
        view.getCommitButton().setOnAction(e -> handleCommitAction());

        view.getCreateRepositoryPopup().setOnAction(e -> {
            view.hidePopup();
            appController.openDialog(new CreateRepositoryDialogController(appController));
        });
        view.getAddRepositoryPopup().setOnAction(e -> {
            view.hidePopup();
            appController.openDialog(new AddRepositoryDialogController(appController));
        });
        view.getCloneRepositoryPopup().setOnAction(e -> {
            view.hidePopup();

            if (ConnectionManager.isNotOnline()) {
                appController.openDialog(new StartGameDialogController(appController));
                return;
            }

            appController.openDialog(new CloneRepositoryDialogController(appController));
        });
    }

    private void switchToChangesTab() {
        view.switchToChangesTab();
        MouseEvent mouseClickEvent = new MouseEvent(
                MouseEvent.MOUSE_CLICKED,
                0, 0, 0, 0,
                MouseButton.PRIMARY,
                1,
                true, true, true, true,
                true, true, true, true, true, true,
                null
        );
        ObservableList<Node> changedFileList = view.getChangedFilesList().getChildren();
        view.clearTextArea();
        view.getMainContent().getChildren().clear();

        if (!changedFileList.isEmpty()) {
            Node button = changedFileList.get(0);
            button.fireEvent(mouseClickEvent);
        } else {
            view.clearTextArea();
        }
    }

    private void switchToHistoryTab() {
        view.switchToHistoryTab();
        MouseEvent mouseClickEvent = new MouseEvent(
                MouseEvent.MOUSE_CLICKED,
                0, 0, 0, 0,
                MouseButton.PRIMARY,
                1,
                true, true, true, true,
                true, true, true, true, true, true,
                null
        );
        ObservableList<Node> historyList = view.getHistoryList().getChildren();
        view.clearTextArea();
        view.getMainContent().getChildren().clear();

        if (!historyList.isEmpty()) {
            Node button = historyList.get(0);
            button.fireEvent(mouseClickEvent);
        } else {
            view.clearTextArea();
        }
    }

    private void setListeners() {
        RepositoryManager.currentRepositoryProperty().addListener((observable, oldRepo, newRepo) -> {
            if (newRepo != null) {
                try {
                    RepositoryManager.setCurrentRepository(newRepo);
                } catch (Exception e) {
                    appController.showNotification("Failed to set current repository.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
            }
            detectAndStageChanges();
            updateHistoryTab();
            updateChangedFilesList();
            switchToChangesTab();
        });

        appController.getFocusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                view.clearTextArea();
                detectAndStageChanges();
                updateChangedFilesList();
                updateHistoryTab();
            }

            if (!newValue) {
                view.hidePopup();
            }
        });

        Repository.getRepositories().addListener((ListChangeListener<? super Repository>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Repository repository : change.getAddedSubList()) {
                        boolean alreadyExists = view.getRepositoryList().getChildren().stream().anyMatch(node -> {
                            if (node instanceof HBox) {
                                HBox buttonContent = (HBox) node;
                                Label label = (Label) buttonContent.getChildren().get(1);
                                return label.getText().equals(repository.getName())
                                        && buttonContent.getUserData() instanceof Repository
                                        && ((Repository) buttonContent.getUserData()).getOwnerId().equals(repository.getOwnerId());
                            }
                            return false;
                        });
                        if (!alreadyExists) {
                            view.getRepositoryList().getChildren().add(ComponentUtility.createListButton(repository, view, ComponentUtility.listButtonType.REPOSITORY));
                        }
                    }
                }
                if (change.wasRemoved()) {
                    for (Repository repo : change.getRemoved()) {
                        view.getRepositoryList().getChildren().removeIf(node -> {
                            if (node instanceof HBox) {
                                HBox buttonContent = (HBox) node;
                                Label label = (Label) buttonContent.getChildren().get(1);
                                return label.getText().equals(repo.getName());
                            }
                            return false;
                        });
                    }
                }
            }
        });

        view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
            List<Repository> filteredRepositories;

            if (newValue.isEmpty()) {
                filteredRepositories = Repository.getRepositories();
            } else {
                final String searchTerm = newValue.toLowerCase();
                filteredRepositories = Repository.getRepositories().stream()
                        .filter(repo -> {
                            String repoName = repo.getName().toLowerCase();
                            String substringToCompare;

                            if (searchTerm.length() > repoName.length()) {
                                substringToCompare = repoName;
                            } else {
                                substringToCompare = repoName.substring(0, searchTerm.length());
                            }

                            int distance = LevenshteinUtility.getLevenshteinDistance(substringToCompare, searchTerm);
                            return distance <= 3;
                        })
                        .collect(Collectors.toList());
            }

            ObservableList<Node> children = view.getRepositoryList().getChildren();
            if (children.size() > 1) {
                children.subList(1, children.size()).clear();
            }

            for (Repository repository : filteredRepositories) {
                view.getRepositoryList().getChildren().add(ComponentUtility.createListButton(repository, view, ComponentUtility.listButtonType.REPOSITORY));
            }
        });
    }

    private void detectAndStageChanges() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        Repository currentRepo = RepositoryManager.getCurrentRepository();

        if (versionControl != null && currentRepo != null) {
            try {
                versionControl.getIndex().detectAndStageChanges(currentRepo);
            } catch (IOException e) {
                appController.showNotification("Failed to detect and stage changes.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            }
        }
    }

    public void updateChangedFilesList() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        view.getChangedFilesList().getChildren().clear();
        view.getTextArea().setParagraphStyle(0, "-fx-background-color: transparent; -fx-fill: white;");
        view.getChangesLabel().setText("0 files changed");
        if (versionControl == null) return;

        Map<String, String> stagedFiles = versionControl.getIndex().getStagedFiles();
        if (stagedFiles == null || stagedFiles.isEmpty()) {
            return;
        }

        Commit currentCommit = versionControl.getCurrentCommit();
        Map<String, String> cumulativeTree = loadCumulativeTree(currentCommit);

        AtomicInteger changedFilesCount = new AtomicInteger(0);
        stagedFiles.forEach((filename, stagedBlobId) -> {
            String committedBlobId = cumulativeTree.get(filename);

            boolean isChanged = !stagedBlobId.equals(committedBlobId);

            if (isChanged) {
                HBox button = ComponentUtility.createListButton(filename, view, ComponentUtility.listButtonType.CHANGES);
                if (button == null) return;

                button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> showFileDifference(filename, stagedBlobId));
                view.getChangedFilesList().getChildren().add(button);
                changedFilesCount.incrementAndGet();
            }
        });

        view.getChangesLabel().setText(changedFilesCount.get() + " files changed");
        switchToChangesTab();
    }

    private Map<String, String> loadCumulativeTree(Commit commit) {
        if (commit == null) return new HashMap<>();

        Map<String, String> cumulativeTree = new HashMap<>();

        Commit currentCommit = commit;
        while (currentCommit != null) {
            Tree currentTree = Tree.loadFromDisk(currentCommit.getTreeId(), RepositoryManager.getCurrentRepository().getObjectsPath());
            cumulativeTree.putAll(currentTree.getEntries());

            String parentId = currentCommit.getParentId();
            currentCommit = (parentId != null) ? Commit.loadFromDisk(parentId, RepositoryManager.getCurrentRepository().getObjectsPath()) : null;
        }

        return cumulativeTree;
    }

    private void resetMultiFunctionButton() {
        view.switchOriginButton(MainLayoutView.OriginType.FETCH);
        view.getOriginButton().setOnMouseClicked(e -> updateMultiFunctionButton());
    }

    private void updateMultiFunctionButton() {
        if (ConnectionManager.isNotOnline()) {
            appController.openDialog(new StartGameDialogController(appController));
            return;
        }

        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            resetMultiFunctionButton();
            return;
        }

        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) {
            appController.showNotification("No version control system found.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            resetMultiFunctionButton();
            return;
        }

        Commit latestLocalCommit = versionControl.getCurrentCommit();
        if (latestLocalCommit == null) {
            appController.showNotification("You are up to date with the upstream branch.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            resetMultiFunctionButton();
            return;
        }

        if (ConnectionManager.isNotOnline()) {
            appController.openDialog(new StartGameDialogController(appController));
            return;
        }

        String latestDatabaseCommitId;
        Commit latestDatabaseCommit;
        try {
            latestDatabaseCommitId = RepositoryRepository.getLatestCommitId(currentRepo);
            latestDatabaseCommit = latestDatabaseCommitId != null
                    ? CommitRepository.load(latestDatabaseCommitId, currentRepo)
                    : null;
        } catch (SQLException e) {
            appController.showNotification("Failed to fetch latest commit from the database.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            resetMultiFunctionButton();
            return;
        }

        boolean hasPendingCommits = latestDatabaseCommit == null
                || latestLocalCommit.getTimestamp().withNano(0).isAfter(latestDatabaseCommit.getTimestamp().withNano(0));

        if (hasPendingCommits) {
            appController.showNotification("Successfully fetched from origin. You have pending commits to push.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            view.switchOriginButton(MainLayoutView.OriginType.PUSH);
            view.getOriginButton().setOnMouseClicked(e -> {
                try {
                    RepositoryManager.getVersionControl().push();
                } catch (Exception ex) {
                    appController.showNotification("Failed to push changes to the database.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
                updateMultiFunctionButton();
                appController.showNotification("Changes pushed successfully.", NotificationBox.NotificationType.SUCCESS, "fas-arrow-up");
            });
        } else if (!latestDatabaseCommitId.equals(latestLocalCommit.getId())) {
            appController.showNotification("Successfully fetched from origin. You are behind the upstream branch.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            view.switchOriginButton(MainLayoutView.OriginType.PULL);
            view.getOriginButton().setOnMouseClicked(e -> {
                try {
                    RepositoryManager.getVersionControl().pull();
                } catch (Exception ex) {
                    appController.showNotification("Failed to pull changes from the database.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
                updateMultiFunctionButton();
                appController.showNotification("Changes pulled successfully.", NotificationBox.NotificationType.SUCCESS, "fas-arrow-down");
            });
        } else {
            appController.showNotification("You are up to date with the upstream branch.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            resetMultiFunctionButton();
        }
    }

    private void handleCommitAction() {
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            return;
        }

        String commitMessage = view.getCommitSummaryTextField().getText() + "\n\n" + view.getCommitDescriptionTextArea();

        VersionControl versionControl = RepositoryManager.getVersionControl();
        try {
            if (!versionControl.commitChanges(commitMessage)) {
                appController.showNotification("No changes to commit.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
                return;
            }
        } catch (IOException e) {
            appController.showNotification("Failed to commit changes.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            return;
        }

        appController.showNotification("Changes committed successfully.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
        view.getChangesLabel().setText("0 files changed");
        view.getCommitSummaryTextField().clear();
        view.getCommitDescriptionTextArea().clear();
        view.clearTextArea();
        updateChangedFilesList();
        updateHistoryTab();
    }

    private void updateHistoryTab() {
        view.getHistoryList().getChildren().clear();
        view.getHistoryLabel().setText("0 commits");
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            return;
        }

        String commitId = currentRepo.getLatestCommitId();
        if (commitId == null) {
            return;
        }

        int commitCount = 0;
        while (commitId != null && !commitId.isBlank()) {
            Commit commit = Commit.loadFromDisk(commitId, currentRepo.getObjectsPath());

            HBox button = ComponentUtility.createListButton(commit.getMessage(), view, ComponentUtility.listButtonType.HISTORY);
            if (button == null) return;

            button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> showCommitDetails(commit));
            view.getHistoryList().getChildren().add(button);

            commitId = commit.getParentId();
            commitCount++;
        }
        view.getHistoryLabel().setText(commitCount + " commits");
    }

    public void showFileDifference(String filename, String blobId) {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) return;

        String newContent = FileUtility.loadFromDisk(blobId, currentRepo.getObjectsPath());
        String oldContent = "";

        Commit latestCommit = versionControl.getCurrentCommit();
        if (latestCommit != null) {
            Tree latestTree = Tree.loadFromDisk(latestCommit.getTreeId(), currentRepo.getObjectsPath());
            String oldBlobId = latestTree.getEntries().get(filename);
            if (oldBlobId != null) {
                oldContent = FileUtility.loadFromDisk(oldBlobId, currentRepo.getObjectsPath());
            }
        }

        List<LineDifference> differences = DifferenceUtility.getDifference(oldContent, newContent);
        view.clearTextArea();
        renderDifferences(differences);
    }

    private void renderDifferences(List<LineDifference> differences) {
        StackPane mainContent = view.getMainContent();
        mainContent.getChildren().clear();
        InlineCssTextArea styledTextArea = view.getTextArea();
        styledTextArea.clear();
        mainContent.getChildren().addAll(styledTextArea, view.getMainContentOverlay());

        for (int i = 0; i < differences.size(); i++) {
            LineDifference line = differences.get(i);

            int start = styledTextArea.getLength();
            styledTextArea.appendText(line.getContent() + "\n");

            String paragraphStyle;
            switch (line.getType()) {
                case ADDED:
                    paragraphStyle = "-fx-background-color: rgba(17,58,27,0.5); " +
                            "-fx-fill: white; " +
                            "-fx-background-insets: 0; " +
                            "-fx-padding: 5px;" +
                            "-fx-font-weight: 500;";
                    break;
                case REMOVED:
                    paragraphStyle = "-fx-background-color: rgba(69,12,15,0.5); " +
                            "-fx-fill: white; " +
                            "-fx-background-insets: 0; " +
                            "-fx-padding: 5px;" +
                            "-fx-font-weight: 500;";
                    break;
                default:
                    paragraphStyle = "-fx-background-color: transparent; " +
                            "-fx-fill: white; " +
                            "-fx-padding: 5px;" +
                            "-fx-font-weight: 500;";
                    break;
            }

            styledTextArea.setParagraphStyle(i, paragraphStyle);

            for (Highlight highlight : line.getHighlights()) {
                styledTextArea.setStyle(
                        start + highlight.start(),
                        start + highlight.end(),
                        "-fx-fill: white; -fx-background-color: rgba(0, 255, 0, 0.6); -fx-font-weight: 500; "
                );
            }
        }
    }

    private void showCommitDetails(Commit commit) {
        StackPane mainContent = view.getMainContent();
        mainContent.getChildren().clear();
        mainContent.getChildren().addAll(view.getHistoryRoot(), view.getMainContentOverlay());

        Repository repository = RepositoryManager.getCurrentRepository();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");
        LocalDateTime timestamp = commit.getTimestamp();

        view.getCommitTitleLabel().setText(commit.getMessage());
        view.getCommitOwnerLabel().setText("Owner");
        view.getCommitIdLabel().setText(commit.getId().substring(0, 7) + "...");
        view.getCommitTimestampLabel().setText(timestamp.format(formatter));

        Tree tree = Tree.loadFromDisk(commit.getTreeId(), RepositoryManager.getCurrentRepository().getObjectsPath());
        view.getHistoryChangedFilesList().getChildren().clear();

        for (Map.Entry<String, String> entry : tree.getEntries().entrySet()) {

            view.getHistoryChangedFilesList().getChildren().add(new Label(entry.getKey() + ": " + entry.getValue()));
//
//            appendStyledLine(
//                    styledTextArea,
//                    "  " + entry.getKey(),
//                    " -> " + entry.getValue(),
//                    "-fx-fill: lightgray;"
//            );
        }
    }

    private void appendStyledLine(InlineCssTextArea textArea, String label, String value, String valueStyle) {
        int labelStart = textArea.getLength();
        textArea.appendText(label);

        textArea.setStyle(labelStart, textArea.getLength(), "-fx-font-weight: bold; -fx-fill: white;");

        int valueStart = textArea.getLength();
        textArea.appendText(value + "\n");

        textArea.setStyle(valueStart, textArea.getLength(), valueStyle);
    }
}