package com.bot;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        WebDriver driver = new SafariDriver();
        driver.get("https://www.chess.com/play/computer");

        // 1. Tenta clicar no Start automaticamente
        try {
            System.out.println("Aguardando o carregamento do botão Start...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement btnStart = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Start')]")
            ));
            btnStart.click();
            System.out.println("Botão Start clicado com sucesso!");
        } catch (Exception e) {
            System.out.println("Não consegui clicar no Start automaticamente. Clique você mesmo.");
        }

        // 2. Pausa para configuração final
        System.out.println("\n--- CONFIGURAÇÃO ---");
        System.out.println("Escolha o bot adversário e clique em 'Choose'.");
        System.out.println("Quando a partida começar, aperte ENTER aqui no terminal.");
        new Scanner(System.in).nextLine();

        Engine engine = new Engine();
        engine.iniciar();

        String ultimoFenJogada = "";
        System.out.println("Bot em campo. Aguardando lances...");

        // 3. Game Loop
        while (true) {
            List<WebElement> pecas = driver.findElements(By.className("piece"));
            String minhaCor = detectarMinhaCor(pecas);
            String fenAtual = gerarFEN(pecas, minhaCor);

            if (!fenAtual.equals(ultimoFenJogada)) {
                if (ehMinhaVez(driver, minhaCor)) {
                    String melhorLance = engine.calcularMelhorLance(fenAtual, 500);
                    if (melhorLance != null) {
                        moverPeca(melhorLance, driver);
                        // Atualiza o estado para evitar lances repetidos
                        ultimoFenJogada = gerarFEN(driver.findElements(By.className("piece")), minhaCor);
                    }
                }
            }
            Thread.sleep(100); 
        }
    }

    private static String gerarFEN(List<WebElement> pecas, String minhaCor) {
        char[][] tabuleiro = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) tabuleiro[i][j] = '-';
        }

        for (WebElement peca : pecas) {
            String[] classes = peca.getAttribute("class").split(" ");
            String tipoPeca = "";
            String posicao = "";

            for (String c : classes) {
                if (c.length() == 2 && (c.startsWith("w") || c.startsWith("b"))) tipoPeca = c;
                if (c.startsWith("square-")) posicao = c.replace("square-", "");
            }

            if (!tipoPeca.isEmpty() && posicao.length() == 2) {
                int coluna = Character.getNumericValue(posicao.charAt(0)) - 1;
                int linha = Character.getNumericValue(posicao.charAt(1)) - 1;
                int linhaFEN = 7 - linha; 
                tabuleiro[linhaFEN][coluna] = mapearPeca(tipoPeca);
            }
        }
        
        String boardFen = montarStringFEN(tabuleiro);
        String corDaVez = (minhaCor != null && minhaCor.equals("b")) ? "b" : "w";
        return boardFen + " " + corDaVez + " KQkq - 0 1";
    }

    private static char mapearPeca(String tipo) {
        char peca = tipo.charAt(1);
        return tipo.charAt(0) == 'w' ? Character.toUpperCase(peca) : peca;
    }

    private static String montarStringFEN(char[][] tabuleiro) {
        StringBuilder fen = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int vazios = 0;
            for (int j = 0; j < 8; j++) {
                if (tabuleiro[i][j] == '-') {
                    vazios++;
                } else {
                    if (vazios > 0) {
                        fen.append(vazios);
                        vazios = 0;
                    }
                    fen.append(tabuleiro[i][j]);
                }
            }
            if (vazios > 0) fen.append(vazios);
            if (i < 7) fen.append("/");
        }
        return fen.toString();
    }

    private static void moverPeca(String lance, WebDriver driver) throws InterruptedException {
        String de = lance.substring(0, 2);
        String para = lance.substring(2, 4);

        String classeOrigem = "square-" + (de.charAt(0) - 'a' + 1) + de.charAt(1);
        String classeDestino = "square-" + (para.charAt(0) - 'a' + 1) + para.charAt(1);

        driver.findElement(By.className(classeOrigem)).click();
        Thread.sleep(150); 
        driver.findElement(By.className(classeDestino)).click();

        if (lance.length() >= 5) {
            lidarComPromocao(lance.charAt(4), driver);
        }
    }

    private static void lidarComPromocao(char peca, WebDriver driver) {
        try {
            Thread.sleep(200);
            driver.findElement(By.cssSelector(".promotion-piece." + (peca == 'q' ? "w" : "b") + peca)).click();
        } catch (Exception ignored) {}
    }

    private static String detectarMinhaCor(List<WebElement> pecas) {
        for (WebElement peca : pecas) {
            String[] classes = peca.getAttribute("class").split(" ");
            String tipoPeca = "";
            String posicao = "";
            for (String c : classes) {
                if (c.length() == 2 && (c.startsWith("w") || c.startsWith("b"))) tipoPeca = c;
                if (c.startsWith("square-")) posicao = c.replace("square-", "");
            }
            if (tipoPeca.equals("wk") && posicao.length() == 2) {
                return Character.getNumericValue(posicao.charAt(1)) <= 2 ? "w" : "b";
            }
        }
        return "w";
    }

    private static boolean ehMinhaVez(WebDriver driver, String minhaCor) {
        try {
            String[] selectors = {".is-player-turn", ".active-player"};
            for (String sel : selectors) {
                if (!driver.findElements(By.cssSelector(sel)).isEmpty()) return true;
            }
        } catch (Exception ignored) {}
        return true;
    }
}