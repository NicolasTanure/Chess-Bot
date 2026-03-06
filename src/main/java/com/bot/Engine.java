package com.bot;

import java.io.*;

public class Engine {
    private Process engineProcess;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public void iniciar() throws IOException {
        // No Mac (Homebrew), o caminho padrão costuma ser apenas "stockfish"
        engineProcess = new ProcessBuilder("stockfish").start();
        reader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
        writer = new OutputStreamWriter(engineProcess.getOutputStream());
    }

    public String calcularMelhorLance(String fen, int tempoEmMs) throws IOException {
        escrever("position fen " + fen);
        escrever("go movetime " + tempoEmMs);

        String linha;
        while ((linha = reader.readLine()) != null) {
            if (linha.startsWith("bestmove")) {
                return linha.split(" ")[1]; // Ex: "bestmove e2e4" -> retorna "e2e4"
            }
        }
        return null;
    }

    private void escrever(String comando) throws IOException {
        writer.write(comando + "\n");
        writer.flush();
    }

    public void parar() throws IOException {
        escrever("quit");
        engineProcess.destroy();
    }
}