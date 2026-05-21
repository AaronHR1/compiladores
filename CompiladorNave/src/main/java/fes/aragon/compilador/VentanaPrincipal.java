package fes.aragon.compilador;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

public class VentanaPrincipal extends Application {

    // UI
    private TextArea editorCodigo;
    private TextArea areaErrores;
    private TableView<String[]> tablaSimbolos;
    private Canvas tablero;
    private GraphicsContext gc;
    private Stage ventana;

    // Nave
    private Image imgDerecha, imgIzquierda, imgArriba, imgAbajo, imgNave;
    private double naveX = 55, naveY = 55;
    private double targetX = 55, targetY = 55;
    private int indice = 0;
    private ArrayList<String> comandos = new ArrayList<>();
    private String comandoActual = "";
    private boolean ejecutando = false;
    private boolean dirArriba, dirAbajo, dirIzquierda, dirDerecha;
    private final int TAM = 50;
    private final int OFFSET = 0;
    private final double VEL = 3;

    @Override
    public void start(Stage stage) {
        this.ventana = stage;
        cargarImagenes();

        // ── Editor ──
        editorCodigo = new TextArea();
        editorCodigo.setFont(javafx.scene.text.Font.font("Monospaced", 14));
        editorCodigo.setPromptText("Escribe tu programa aquí...");
        editorCodigo.setText("inicio 1 1\nderecha\nmover 3\nabajo\nmover 2\nfin");

        VBox panelEditor = new VBox();
        Label lblEditor = new Label(" Editor de código");
        lblEditor.setStyle("-fx-background-color:#f0f0f0;-fx-padding:6 10;-fx-font-size:12;-fx-border-color:#ddd;-fx-border-width:0 0 1 0;");
        lblEditor.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(editorCodigo, Priority.ALWAYS);
        panelEditor.getChildren().addAll(lblEditor, editorCodigo);

        // ── Tablero ──
        tablero = new Canvas(500, 500);
        gc = tablero.getGraphicsContext2D();
        dibujarTablero();

        VBox panelTablero = new VBox();
        Label lblTablero = new Label(" Tablero");
        lblTablero.setStyle("-fx-background-color:#f0f0f0;-fx-padding:6 10;-fx-font-size:12;-fx-border-color:#ddd;-fx-border-width:0 0 1 0;");
        lblTablero.setMaxWidth(Double.MAX_VALUE);
        panelTablero.getChildren().addAll(lblTablero, tablero);

        SplitPane splitPrincipal = new SplitPane();
        splitPrincipal.setOrientation(Orientation.HORIZONTAL);
        splitPrincipal.getItems().addAll(panelEditor, panelTablero);
        splitPrincipal.setDividerPositions(0.4);

        // ── Panel errores ──
        areaErrores = new TextArea();
        areaErrores.setEditable(false);
        areaErrores.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        areaErrores.setPrefHeight(130);
        areaErrores.setStyle("-fx-control-inner-background:#1e1e1e;-fx-text-fill:#ff6b6b;");

        VBox panelErrores = new VBox();
        Label lblErrores = new Label(" Errores");
        lblErrores.setStyle("-fx-background-color:#f0f0f0;-fx-padding:6 10;-fx-font-size:12;-fx-border-color:#ddd;-fx-border-width:0 0 1 0;");
        lblErrores.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(areaErrores, Priority.ALWAYS);
        panelErrores.getChildren().addAll(lblErrores, areaErrores);

        // ── Tabla de símbolos ──
        tablaSimbolos = new TableView<>();
        TableColumn<String[], String> colNombre = new TableColumn<>("Variable");
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        colNombre.setPrefWidth(100);
        TableColumn<String[], String> colValor = new TableColumn<>("Valor inicial");
        colValor.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        colValor.setPrefWidth(100);
        TableColumn<String[], String> colTipo = new TableColumn<>("Valor final");
        colTipo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        colTipo.setPrefWidth(100);
        tablaSimbolos.getColumns().addAll(colNombre, colValor, colTipo);
        tablaSimbolos.setPrefHeight(130);

        VBox panelSimbolos = new VBox();
        Label lblSimbolos = new Label(" Tabla de símbolos");
        lblSimbolos.setStyle("-fx-background-color:#f0f0f0;-fx-padding:6 10;-fx-font-size:12;-fx-border-color:#ddd;-fx-border-width:0 0 1 0;");
        lblSimbolos.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(tablaSimbolos, Priority.ALWAYS);
        panelSimbolos.getChildren().addAll(lblSimbolos, tablaSimbolos);

        SplitPane splitInferior = new SplitPane();
        splitInferior.getItems().addAll(panelErrores, panelSimbolos);
        splitInferior.setDividerPositions(0.5);
        splitInferior.setPrefHeight(150);

        // ── Toolbar ──
        Button btnCompilar = new Button("▶ Compilar y ejecutar");
        btnCompilar.setStyle("-fx-background-color:#3a7bd5;-fx-text-fill:white;-fx-font-size:13;-fx-padding:7 18;");
        btnCompilar.setOnAction(e -> compilarYEjecutar());

        Button btnAbrir = new Button("📂 Abrir");
        btnAbrir.setOnAction(e -> abrirArchivo());
        Button btnGuardar = new Button("💾 Guardar");
        btnGuardar.setOnAction(e -> guardarArchivo());

        Label lblTitulo = new Label("🚀 Compilador Nave");
        lblTitulo.setStyle("-fx-font-size:15;-fx-font-weight:bold;");
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        HBox toolbar = new HBox(10, lblTitulo, esp, btnAbrir, btnGuardar, btnCompilar);
        toolbar.setPadding(new Insets(8, 14, 8, 14));
        toolbar.setStyle("-fx-background-color:#f8f8f8;-fx-border-color:#ddd;-fx-border-width:0 0 1 0;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── Layout final ──
        VBox raiz = new VBox();
        VBox.setVgrow(splitPrincipal, Priority.ALWAYS);
        raiz.getChildren().addAll(toolbar, splitPrincipal, splitInferior);

        Scene escena = new Scene(raiz, 1100, 720);
        stage.setScene(escena);
        stage.setTitle("Compilador Nave");
        stage.show();

        // ── Loop de animación ──
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (ejecutando) {
                    logicaNave();
                    dibujarTablero();
                    dibujarNave();
                }
            }
        };
        timer.start();
    }

    private void cargarImagenes() {
        imgDerecha   = cargarImg("derecha.png");
        imgIzquierda = cargarImg("izquierda.png");
        imgArriba    = cargarImg("arriba.png");
        imgAbajo     = cargarImg("abajo.png");
        imgNave = imgDerecha;
    }

    private Image cargarImg(String nombre) {
        InputStream is = getClass().getResourceAsStream(
                "/fes/aragon/compilador/" + nombre);
        return is != null ? new Image(is) : null;
    }

    private void dibujarTablero() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 500, 500);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                gc.strokeRect(OFFSET + j * TAM, OFFSET + i * TAM, TAM, TAM);
    }

    private void dibujarNave() {
        if (imgNave != null)
            gc.drawImage(imgNave, naveX, naveY, TAM, TAM);
    }

    private void logicaNave() {
        switch (comandoActual) {
            case "arriba": case "abajo": case "izquierda": case "derecha":
                indice++;
                ejecutarSiguiente();
                break;
            case "coloca":
                if (Math.abs(naveX - targetX) > VEL) {
                    naveX += (naveX < targetX) ? VEL : -VEL;
                } else if (Math.abs(naveY - targetY) > VEL) {
                    naveY += (naveY < targetY) ? VEL : -VEL;
                } else {
                    naveX = targetX; naveY = targetY;
                    indice++; ejecutarSiguiente();
                }
                break;
            case "mover":
                boolean llegó = false;
                if (dirArriba)    { naveY -= VEL; llegó = naveY <= targetY; }
                if (dirAbajo)     { naveY += VEL; llegó = naveY >= targetY; }
                if (dirIzquierda) { naveX -= VEL; llegó = naveX <= targetX; }
                if (dirDerecha)   { naveX += VEL; llegó = naveX >= targetX; }
                if (llegó) {
                    naveX = targetX; naveY = targetY;
                    indice++; ejecutarSiguiente();
                }
                break;
            default:
                ejecutando = false;
        }
    }

    private void ejecutarSiguiente() {
        if (indice >= comandos.size()) {
            ejecutando = false;
            return;
        }
        String[] partes = comandos.get(indice).split(" ");
        switch (partes[0]) {
            case "coloca":
                int col = Integer.parseInt(partes[1]);
                int fila = Integer.parseInt(partes[2]);
                targetX = OFFSET + (col - 1) * TAM;
                targetY = OFFSET + (fila - 1) * TAM;
                imgNave = imgDerecha;
                comandoActual = "coloca";
                break;
            case "arriba":
                dirArriba=true; dirAbajo=false; dirIzquierda=false; dirDerecha=false;
                imgNave = imgArriba;
                comandoActual = "arriba";
                break;
            case "abajo":
                dirArriba=false; dirAbajo=true; dirIzquierda=false; dirDerecha=false;
                imgNave = imgAbajo;
                comandoActual = "abajo";
                break;
            case "izquierda":
                dirArriba=false; dirAbajo=false; dirIzquierda=true; dirDerecha=false;
                imgNave = imgIzquierda;
                comandoActual = "izquierda";
                break;
            case "derecha":
                dirArriba=false; dirAbajo=false; dirIzquierda=false; dirDerecha=true;
                imgNave = imgDerecha;
                comandoActual = "derecha";
                break;
            case "mover":
                int pasos = Integer.parseInt(partes[1]);
                if (dirArriba)    targetY = naveY - pasos * TAM;
                if (dirAbajo)     targetY = naveY + pasos * TAM;
                if (dirIzquierda) targetX = naveX - pasos * TAM;
                if (dirDerecha)   targetX = naveX + pasos * TAM;
                comandoActual = "mover";
                break;
            case "fin":
                ejecutando = false;
                break;
        }
    }

    private void compilarYEjecutar() {
        try {
            // 1. Guardar código temporal
            Files.writeString(Path.of("programa_temp.nave"), editorCodigo.getText());

            // 2. Limpiar estado
            areaErrores.setText("");
            tablaSimbolos.getItems().clear();
            CUP$parser$actions.salida.clear();
            CUP$parser$actions.pilaBucle.clear();

            // 3. Compilar
            parser.compilar("programa_temp.nave", "salida.fes");

            // 4. Mostrar éxito
            areaErrores.setStyle("-fx-control-inner-background:#1a2e1a;-fx-text-fill:#6bff6b;");
            areaErrores.setText("✔ Compilación exitosa\n");
            areaErrores.appendText("Instrucciones generadas: "
                    + CUP$parser$actions.salida.size() + "\n");

            // 5. Llenar tabla de símbolos con variables de bucles
            for (String[] sym : TablaSimbolosHelper.getSimbolos()) {
                tablaSimbolos.getItems().add(sym);
            }

            // 6. Iniciar animación
            comandos = new ArrayList<>(CUP$parser$actions.salida);
            indice = 0;
            naveX = 0; naveY = 0;
            targetX = 0; targetY = 0;
            dirDerecha = true;
            dirArriba = dirAbajo = dirIzquierda = false;
            imgNave = imgDerecha;
            ejecutando = true;
            ejecutarSiguiente();

        } catch (Exception ex) {
            areaErrores.setStyle("-fx-control-inner-background:#1e1e1e;-fx-text-fill:#ff6b6b;");
            areaErrores.setText("✘ Error: " + ex.getMessage());
        }
    }

    private void abrirArchivo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Abrir programa");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos nave", "*.nave"));
        File f = fc.showOpenDialog(ventana);
        if (f != null) {
            try { editorCodigo.setText(Files.readString(f.toPath())); }
            catch (Exception ex) { areaErrores.setText("Error: " + ex.getMessage()); }
        }
    }

    private void guardarArchivo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar programa");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos nave", "*.nave"));
        File f = fc.showSaveDialog(ventana);
        if (f != null) {
            try { Files.writeString(f.toPath(), editorCodigo.getText()); }
            catch (Exception ex) { areaErrores.setText("Error: " + ex.getMessage()); }
        }
    }

    public static void main(String[] args) { launch(args); }
}