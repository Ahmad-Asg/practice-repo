package edu.persian.ui;

import edu.persian.application.LearningStudioFacade;
import edu.persian.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Locale;

public final class ChapterScreen extends BorderPane {
    private final LearningStudioFacade studio;
    private final ListView<Chapter> list=new ListView<>();
    private final ObservableList<Chapter> chapters=FXCollections.observableArrayList();
    private final FilteredList<Chapter> visibleChapters=new FilteredList<>(chapters);
    private final TextField search=new TextField();
    private final Label chapterCount=new Label();
    private final VBox detail=Ui.box();
    private final Label feedback=Ui.message();

    public ChapterScreen(LearningStudioFacade studio) {
        this.studio=java.util.Objects.requireNonNull(studio,"studio");
        Label brand=new Label("Persian Learning Studio"); brand.getStyleClass().add("brand");
        Label subtitle=new Label("Chapters, lessons and vocabulary · فارسی اور اردو"); subtitle.getStyleClass().add("banner-subtitle");
        VBox masthead=new VBox(5,brand,subtitle); masthead.getStyleClass().add("masthead");
        HBox banner=new HBox(masthead); banner.getStyleClass().add("banner"); setTop(banner);
        list.setItems(visibleChapters);
        list.setPlaceholder(new Label("No chapters yet\nپہلا باب بنائیں"));
        list.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(Chapter chapter, boolean empty) {
                super.updateItem(chapter,empty);
                if(empty || chapter==null) { setText(null); setGraphic(null); return; }
                Label name=new Label(chapter.title()); name.setWrapText(true); name.getStyleClass().add("chapter-name");
                Label number=new Label("Chapter "+chapter.id()); number.getStyleClass().add("chapter-number");
                VBox item=new VBox(3,name,number); setGraphic(item); setText(null);
            }
        });
        list.getSelectionModel().selectedItemProperty().addListener((o,old,value) -> Ui.guard(feedback,this::render));
        Label sidebarTitle=Ui.title("Chapters · ابواب");
        chapterCount.getStyleClass().add("muted");
        Button create=Ui.primaryButton("+ New chapter · نیا باب",() -> editTitle(null)); create.setMaxWidth(Double.MAX_VALUE);
        search.setPromptText("Find a chapter"); Ui.leftEntry(search); search.getStyleClass().add("search-field");
        search.textProperty().addListener((o,old,term) -> {
            String query=term.strip().toLowerCase(Locale.ROOT);
            visibleChapters.setPredicate(chapter -> query.isEmpty()
                || chapter.title().toLowerCase(Locale.ROOT).contains(query)
                || Long.toString(chapter.id()).contains(query));
            list.setPlaceholder(new Label(query.isEmpty() ? "No chapters yet · ابھی کوئی باب نہیں" : "No matching chapters · کوئی باب نہیں ملا"));
            updateCount();
        });
        VBox sidebar=new VBox(8,sidebarTitle,chapterCount,create,search,list); sidebar.getStyleClass().add("sidebar");
        VBox.setMargin(create,new Insets(14,0,8,0));
        VBox.setMargin(search,new Insets(4,0,8,0));
        sidebar.setPrefWidth(300); VBox.setVgrow(list,Priority.ALWAYS); setLeft(sidebar);
        detail.getStyleClass().add("detail");
        ScrollPane scroll=new ScrollPane(detail); scroll.setFitToWidth(true); scroll.getStyleClass().add("content-scroll"); setCenter(scroll);
        HBox footer=new HBox(feedback); footer.getStyleClass().add("footer"); setBottom(footer);
        reload(null);
    }
    private void reload(Long selected) {
        chapters.setAll(studio.listChapters());
        if(selected!=null) {
            search.clear();
            visibleChapters.stream().filter(c -> c.id()==selected).findFirst().ifPresent(c -> list.getSelectionModel().select(c));
        }
        updateCount();
        render();
    }
    private void updateCount() {
        int total=chapters.size(), shown=visibleChapters.size();
        search.setVisible(total>0); search.setManaged(total>0);
        chapterCount.setText(search.getText().isBlank()
            ? total+" "+(total==1 ? "chapter" : "chapters")
            : shown+" found");
    }
    private void editTitle(Chapter chapter) {
        TextField title=new TextField(chapter==null ? "" : chapter.title()); Ui.leftEntry(title);
        title.setPromptText("Enter a chapter title");
        Label error=Ui.message(); VBox form=Ui.box(new Label("Chapter title · باب کا عنوان"),title,error); form.setPrefWidth(560);
        Ui.saveDialog(this,chapter==null ? "New chapter · نیا باب" : "Rename chapter · عنوان بدلیں",form,error,() -> {
            long id;
            if(chapter==null) id=studio.createChapter(title.getText()).id();
            else { studio.renameChapter(chapter.id(),title.getText()); id=chapter.id(); }
            reload(id);
        });
    }
    private void render() {
        detail.getChildren().clear(); Chapter selected=list.getSelectionModel().getSelectedItem();
        if(selected==null) {
            boolean first=chapters.isEmpty();
            Label welcome=Ui.title(first ? "Create your first chapter" : "Select a chapter"); welcome.getStyleClass().add("hero-title");
            Label help=new Label(first
                ? "Give the chapter a title. Then choose its sections and add lesson text.\nباب کا عنوان لکھیں، پھر اجزا اور متن شامل کریں۔"
                : "Choose a chapter from the list to edit its sections, text or glossary.\nترمیم کے لیے بائیں فہرست سے باب منتخب کریں۔");
            help.setWrapText(true); help.getStyleClass().add("hero-copy");
            VBox empty=Ui.box(welcome,help); empty.getStyleClass().add("empty-state");
            if(first) empty.getChildren().add(Ui.primaryButton("+ New chapter · نیا باب",() -> editTitle(null)));
            detail.getChildren().add(empty); return;
        }
        long id=selected.id(); Chapter chapter=studio.chapter(id);
        var types=studio.structure(id);
        Label eyebrow=new Label("Chapter "+id); eyebrow.getStyleClass().add("eyebrow");
        Label title=Ui.title(chapter.title()); title.getStyleClass().add("chapter-title"); title.setMinWidth(0);
        boolean complete=studio.isComplete(id);
        Label status=new Label(complete ? "●  Complete · مکمل" : "●  Draft · نامکمل"); status.getStyleClass().addAll("status",complete ? "complete" : "draft");
        HBox titleRow=new HBox(18,title,status); titleRow.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(title,Priority.ALWAYS);
        Label guidance=new Label(complete ? "All selected sections have content."
            : types.isEmpty() ? "Choose sections to get started."
            : "Fill each selected section to complete this chapter."); guidance.getStyleClass().add("muted");
        FlowPane actions=new FlowPane(8,8);
        actions.getChildren().addAll(Ui.button("Rename · عنوان",() -> editTitle(chapter)),
            Ui.primaryButton("Structure · ترتیب",() -> Ui.guard(feedback,() -> StructureScreen.open(this,id,studio,this::render))),
            Ui.button("Glossary · فرہنگ",() -> Ui.guard(feedback,() -> GlossaryScreen.open(this,id,studio,this::render))),
            Ui.dangerButton("Delete chapter · باب حذف",() -> {
                if(Ui.confirm(this,"Delete ‘"+chapter.title()+"’? All its components, saved text and glossary entries will also be deleted.\nباب، تمام اجزا، محفوظ متن اور فرہنگ کے اندراجات بھی حذف ہوں گے۔","Delete chapter · باب حذف"))
                    Ui.run(feedback,() -> { studio.deleteChapter(id,true); reload(null); });
            }));
        VBox header=new VBox(12,eyebrow,titleRow,guidance,actions); header.getStyleClass().add("chapter-header");
        detail.getChildren().add(header);
        if(types.isEmpty()) {
            Label hint=Ui.title("Choose sections for this chapter"); hint.getStyleClass().add("section-title");
            Label explanation=new Label("Add a dialogue, reading, explanation or other section to get started.\nشروع کرنے کے لیے مکالمہ، عبارت، تشریح یا دوسرا جزو شامل کریں۔"); explanation.setWrapText(true); explanation.getStyleClass().add("muted");
            VBox start=Ui.box(hint,explanation,Ui.primaryButton("Choose sections · اجزا منتخب کریں",() -> Ui.guard(feedback,() -> StructureScreen.open(this,id,studio,this::render))));
            start.getStyleClass().add("empty-section"); detail.getChildren().add(start);
        } else {
            Label sections=new Label("Sections  ·  "+types.size()); sections.getStyleClass().add("section-list-title"); detail.getChildren().add(sections);
        }
        int position=0;
        for(ComponentType type:types) {
            detail.getChildren().add(type==ComponentType.GLOSSARY
                ? GlossaryScreen.card(this,id,++position,studio,this::render,feedback)
                : ContentScreen.card(this,id,type,++position,studio,this::render,feedback));
        }
    }
}
