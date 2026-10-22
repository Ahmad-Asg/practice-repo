package edu.persian.ui;

import edu.persian.application.LearningStudioFacade;
import edu.persian.model.ComponentType;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public final class StructureScreen {
    private StructureScreen() { }
    public static void open(Node owner,long chapter,LearningStudioFacade studio,Runnable refresh) {
        ListView<ComponentType> selected=new ListView<>(); selected.getItems().setAll(studio.structure(chapter)); selected.setPrefHeight(270);
        ComboBox<ComponentType> available=new ComboBox<>(); available.getItems().setAll(ComponentType.values()); available.getSelectionModel().selectFirst(); available.setMaxWidth(Double.MAX_VALUE);
        Label feedback=Ui.message();
        Button add=Ui.primaryButton("Add section · جزو شامل کریں",() -> {
            ComponentType type=available.getValue();
            if (!selected.getItems().contains(type)) { selected.getItems().add(type); selected.getSelectionModel().select(type); feedback.setText(""); }
            else feedback.setText("Already selected · پہلے سے منتخب ہے");
        });
        Button up=Ui.button("↑ Move up · اوپر",() -> move(selected,-1));
        Button down=Ui.button("↓ Move down · نیچے",() -> move(selected,1));
        Button remove=Ui.button("Remove section · جزو ہٹائیں",() -> { int i=selected.getSelectionModel().getSelectedIndex(); if(i>=0) selected.getItems().remove(i); });
        Runnable updateButtons=() -> {
            int index=selected.getSelectionModel().getSelectedIndex();
            up.setDisable(index<=0); down.setDisable(index<0 || index>=selected.getItems().size()-1); remove.setDisable(index<0);
        };
        selected.getSelectionModel().selectedIndexProperty().addListener((o,old,value) -> updateButtons.run());
        selected.getItems().addListener((javafx.collections.ListChangeListener<ComponentType>) change -> updateButtons.run());
        updateButtons.run();
        HBox addRow=Ui.row(available,add); HBox.setHgrow(available,Priority.ALWAYS);
        VBox form=Ui.box(Ui.title("Chapter structure · باب کی ترتیب"),
                new Label("Choose the sections you want, then put them in reading order. · اجزا منتخب کرکے ترتیب دیں۔"),
                new Label("Add a section · جزو شامل کریں"),addRow,
                new Label("Sections in this chapter · اس باب کے اجزا"),selected,
                Ui.row(up,down,remove),
                new Label("Removed sections keep their saved text if you add them again. · ہٹائے گئے جزو کا متن محفوظ رہتا ہے۔"),feedback);
        form.setPrefWidth(650);
        Ui.saveDialog(owner,"Structure · ترتیب",form,feedback,() -> { studio.saveStructure(chapter,new ArrayList<>(selected.getItems())); refresh.run(); });
    }
    private static void move(ListView<ComponentType> list,int offset) {
        int index=list.getSelectionModel().getSelectedIndex(), target=index+offset;
        if(index<0 || target<0 || target>=list.getItems().size()) return;
        ComponentType type=list.getItems().remove(index); list.getItems().add(target,type); list.getSelectionModel().select(target);
    }
}
