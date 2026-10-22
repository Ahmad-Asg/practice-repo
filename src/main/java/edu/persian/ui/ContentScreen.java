package edu.persian.ui;

import edu.persian.application.LearningStudioFacade;
import edu.persian.model.ComponentType;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class ContentScreen {
    private ContentScreen() { }
    static VBox card(Node owner, long chapter, ComponentType type, int position,
                     LearningStudioFacade studio, Runnable refresh, Label feedback) {
        VBox card=Ui.sectionCard(type.toString(),position);
        String saved=studio.content(chapter,type);
        Label preview=new Label(saved.isEmpty() ? "No content yet · ابھی متن موجود نہیں" : saved);
        preview.setWrapText(true); preview.setMaxWidth(Double.MAX_VALUE); Ui.rtl(preview);
        preview.getStyleClass().add(saved.isEmpty() ? "empty-preview" : "content-preview");
        card.getChildren().addAll(preview,Ui.row(
            Ui.button("Edit content · متن لکھیں",() -> Ui.guard(feedback,() -> open(owner,chapter,type,studio,refresh))),
            Ui.button("Remove content · متن حذف",() -> {
                if(Ui.confirm(owner,"Remove this section's saved text? The chapter will become incomplete.\nاس جزو کا متن حذف کریں؟ باب نامکمل ہو جائے گا۔","Remove text · متن حذف"))
                    Ui.run(feedback,() -> { studio.removeContent(chapter,type); refresh.run(); });
            })));
        return card;
    }
    public static void open(Node owner,long chapter,ComponentType type,LearningStudioFacade studio,Runnable refresh) {
        TextArea editor=new TextArea(studio.content(chapter,type)); editor.setWrapText(true); editor.setPrefRowCount(15); Ui.leftEntry(editor);
        Label feedback=Ui.message();
        VBox form=Ui.box(Ui.title(type.toString()),new Label("Text for this section · اس جزو کا متن"),editor,
            new Label("Write your lesson text here. Saving an empty section is not allowed.\nیہاں سبق کا متن لکھیں۔ خالی متن محفوظ نہیں ہو سکتا۔"),feedback);
        form.setPrefWidth(720); VBox.setVgrow(editor,javafx.scene.layout.Priority.ALWAYS);
        Ui.saveDialog(owner,"Content editor · متن کی تدوین",form,feedback,() -> { studio.saveContent(chapter,type,editor.getText()); refresh.run(); });
    }
}
