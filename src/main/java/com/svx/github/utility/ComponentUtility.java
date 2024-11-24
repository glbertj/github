package com.svx.github.utility;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.Repository;
import com.svx.github.view.MainLayoutView;
import com.svx.github.view.dialog.CloneRepositoryDialogView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class ComponentUtility {

    public static HBox createListButton(Object item, Object view, listButtonType type) {
        FontIcon icon = new FontIcon();
        icon.getStyleClass().add("icon");

        Label label = new Label();
        label.getStyleClass().add("primary-text");

        HBox button = new HBox();
        button.getStyleClass().add("list-button");
        button.setUserData(item);

        switch (type) {
            case REPOSITORY -> {
                if (item instanceof Repository && view instanceof MainLayoutView) {
                    icon.setIconLiteral("fab-git-alt");
                    label.setText(((Repository) item).getName());

                    button.getChildren().addAll(icon, label);
                    button.setOnMouseClicked(e -> {
                        RepositoryManager.setCurrentRepository((Repository) item);
                        ((MainLayoutView) view).switchSideBar();
                    });
                }
            }
            case CLONE_REPOSITORY_DIALOG -> {
                if (item instanceof Repository && view instanceof CloneRepositoryDialogView) {
                    icon.setIconLiteral("fab-git-alt");
                    label.setText(((Repository) item).getName());

                    button.getChildren().addAll(icon, label);
                    button.setOnMouseClicked(e -> {
                        ((CloneRepositoryDialogView) view).getRepositoryList().getChildren().forEach(node -> {
                            if (node instanceof HBox hbox) {
                                hbox.getStyleClass().remove("active");
                            }
                        });

                        button.getStyleClass().add("active");
                    });
                }
            }
            case CHANGES -> {
                if (item instanceof String) {
                    label.setText((String) item);
                    button.getChildren().addAll(label);
                }

                button.setOnMouseClicked(e -> {
                    ((MainLayoutView) view).getChangedFilesList().getChildren().forEach(node -> {
                        if (node instanceof HBox hbox) {
                            hbox.getStyleClass().remove("active");
                        }
                    });

                    button.getStyleClass().add("active");
                });
            }
            case HISTORY -> {
                if (item instanceof String) {
                    label.setText((String) item);
                    button.getChildren().addAll(label);
                }

                button.setOnMouseClicked(e -> {
                    ((MainLayoutView) view).getHistoryList().getChildren().forEach(node -> {
                        if (node instanceof HBox hbox) {
                            hbox.getStyleClass().remove("active");
                        }
                    });

                    button.getStyleClass().add("active");
                });
            }
            default -> {
                return null;
            }
        }

        return button;
    }

    public enum listButtonType {
        REPOSITORY,
        CLONE_REPOSITORY_DIALOG,
        CHANGES,
        HISTORY,
    }
}
