import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class Game extends JPanel implements ActionListener, KeyListener {

    int naveX = 250, naveY = 400;
    int naveLarg = 40, naveAlt = 40;
    Image naveImg, asteroideImg, fundoImg;
    int fundoY = 0;
    ArrayList<Asteroide> asteroides;
    Timer timer;
    boolean moveEsquerda, moveDireita, moveCima, moveBaixo;
    boolean running = true;
    Explosao explosao;
    int vida = 3; // Contador de vidas
    int duracaoJogo = 40; // Duração do jogo em segundos
    long tempoInicio; // Hora de início do jogo

    public Game() {
        this.setFocusable(true);
        this.addKeyListener(this);

        fundoImg = new ImageIcon("sprites/space.png").getImage();
        naveImg = new ImageIcon("sprites/navePadrao2.png").getImage();
        asteroideImg = new ImageIcon("sprites/asteroide.png").getImage();

        asteroides = new ArrayList<>();
        timer = new Timer(30, this); // Atualiza o jogo a cada 30 ms
        timer.start();

        tempoInicio = System.currentTimeMillis();

        moveEsquerda = moveDireita = moveCima = moveBaixo = false;

        tocarMusica("music/Ayers-Rock.wav");

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Desenhar fundo rolante
        g.drawImage(fundoImg, 0, fundoY, getWidth(), getHeight(), this);
        g.drawImage(fundoImg, 0, fundoY - getHeight(), getWidth(), getHeight(), this);


        // Desenhar nave
        if (explosao == null && vida > 0) { // Ddesenha a nave se não houver explosão em andamento
            g.drawImage(naveImg, naveX, naveY, naveLarg, naveAlt, this);
        }


        // Desenhar asteroides
        for (Asteroide asteroide : asteroides) {
            g.drawImage(asteroideImg, asteroide.x, asteroide.y, asteroide.width, asteroide.height, this);
        }


        // Desenhar a explosão
        if (explosao != null) {
            explosao.draw(g);
        }

        // Desenhar tempo restante
        g.setColor(Color.WHITE);
        int tempoCorrido = (int) ((System.currentTimeMillis() - tempoInicio) / 1000);
        int tempoRestante = duracaoJogo - tempoCorrido;
        g.drawString("Tempo restante: " + tempoRestante, 10, 50);


        // Desenhar contagem de vidas
        g.setColor(Color.RED);
        g.setFont(new Font("Serif", Font.PLAIN, 20));
        g.drawString("Vidas: " + vida, 10, 20);
    }


    public Clip clip;
    public void tocarMusica(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Para tocar em loop
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void efeitoSom(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            Clip soundClip = AudioSystem.getClip();
            soundClip.open(audioInputStream);
            soundClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pararMusica() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    public void spawnAsteroide() {
        int posicaoX = (int) (Math.random() * getWidth());
        int velocidade = (int) (Math.random() * 5) + 2;
        Asteroide newAsteroide = new Asteroide(posicaoX, -50, velocidade);
        asteroides.add(newAsteroide);
    }

    public void checarColisao() {
        for (Asteroide asteroide : asteroides) {
            if (naveX < asteroide.x + asteroide.width &&
                    naveX + naveLarg > asteroide.x &&
                    naveY < asteroide.y + asteroide.height &&
                    naveY + naveAlt > asteroide.y) {

                vida--; // Reduz a vida
                explosao = new Explosao(naveX, naveY, this); // Inicia a explosão
                asteroides.remove(asteroide);

                efeitoSom("music/explosion2.wav");

                if (vida <= 0) {
                    // Cria um Timer para esperar antes de chamar gameOver()
                    Timer timer = new Timer(200, e -> gameOver()); // Espera 200ms
                    timer.setRepeats(false); // Não repetir
                    timer.start(); // Inicia o timer
                }

                break;
            }
        }
    }

    public void checarVitoria(){
        int tempoCorrido = (int) ((System.currentTimeMillis() - tempoInicio) / 1000);
        if (tempoCorrido > duracaoJogo){
            running = false;
            pararMusica();
            efeitoSom("music/victory.wav");
            JOptionPane.showMessageDialog(this, "Vitória!");
            System.exit(0);
        }
    }





    private void gameOver() {
        pararMusica(); // Para a música ao acabar o jogo
        efeitoSom("music/defeat.wav");
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over!");
        System.exit(0);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (explosao != null) {
            explosao.update();
        }

        fundoY += 1;
        if (fundoY >= getHeight()){
            fundoY = 0;
        }

        // Atualiza posição dos asteroides
        for (Asteroide asteroide : asteroides) {
            asteroide.y += asteroide.velocidade;
        }

        // Remove asteroides que saíram da tela
        asteroides.removeIf(asteroide -> asteroide.y > getHeight());

        // Spawna novos asteroides com intervalo
        if (Math.random() < 0.1) { // Controla a frequência de aparecimento
            spawnAsteroide();
        }

        // Atualiza a posição da nave
        if (moveEsquerda && naveX > 0) naveX -= 5;
        if (moveDireita && naveX + naveLarg < getWidth()) naveX += 5;
        if (moveCima && naveY > 0) naveY -= 5;
        if (moveBaixo && naveY + naveAlt < getHeight()) naveY += 5;

        // Verifica colisões
        checarColisao();

        checarVitoria();

        // Atualiza tela
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> moveEsquerda = true;
            case KeyEvent.VK_RIGHT -> moveDireita = true;
            case KeyEvent.VK_UP -> moveCima = true;
            case KeyEvent.VK_DOWN -> moveBaixo = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> moveEsquerda = false;
            case KeyEvent.VK_RIGHT -> moveDireita = false;
            case KeyEvent.VK_UP -> moveCima = false;
            case KeyEvent.VK_DOWN -> moveBaixo = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Nave");
        Game game = new Game();
        frame.add(game);
        frame.setSize(600, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

    class Asteroide {
        int x, y, velocidade;
        int width = 30;
        int height = 30;

        public Asteroide(int x, int y, int velocidade) {
            this.x = x;
            this.y = y;
            this.velocidade = velocidade;
        }
    }

    class Explosao {
        int x, y;
        int width = 60, height = 60;
        Image[] frames;
        int currentFrame = 0;
        int frameDuracao = 24; // Duração de cada frame em milissegundos
        long lastFrameTime;
        long startTime;
        int duration = 400; // Duração total da explosão
        Game game;

    public Explosao(int x, int y, Game game) {
        this.x = x;
        this.y = y;
        this.game = game;
        this.startTime = System.currentTimeMillis();
        this.frames = loadFrames(); // Carrega os frames da animação
        this.lastFrameTime = System.currentTimeMillis();
    }

    private Image[] loadFrames() {
        int numFrames = 21; // Número de quadros na animação
        Image[] frames = new Image[numFrames];

        for (int i = 0; i < numFrames; i++) {
            try {
                frames[i] = ImageIO.read(new File("sprites/" + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return frames;
    }

    public boolean isActive() {
        return System.currentTimeMillis() - startTime < duration;
    }

    public void update() {
        if (!isActive()) {
            game.explosao = null;
        } else if (System.currentTimeMillis() - lastFrameTime > frameDuracao) {
            currentFrame = (currentFrame + 1) % frames.length;
            lastFrameTime = System.currentTimeMillis();
        }
    }

    public void draw(Graphics g) {
        if (isActive() && frames[currentFrame] != null) {
            g.drawImage(frames[currentFrame], x, y, width, height, null);
        }
    }
}