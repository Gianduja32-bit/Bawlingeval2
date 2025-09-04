package com.estiam.tarbes.cours_jee;

import java.util.ArrayList;
import java.util.List;

public class BowlingGame {
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;

    public BowlingGame() {
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
    }

    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    public void roll(int pins) {
        if (gameOver) return;
        
        if (pins < 0 || pins > 10) {
            throw new IllegalArgumentException("Le nombre de quilles doit être entre 0 et 10");
        }

        Player currentPlayer = players.get(currentPlayerIndex);
        Frame currentFrame = currentPlayer.getCurrentFrame();
        
        // Vérifier si le lancer est valide
        if (!currentFrame.isLastFrame()) {
            if (currentFrame.getRolls().isEmpty()) {
                // Premier lancer de la frame
                if (pins > 10) {
                    throw new IllegalArgumentException("Le nombre de quilles ne peut pas dépasser 10");
                }
            } else if (currentFrame.getRolls().size() == 1) {
                // Deuxième lancer de la frame
                int firstRoll = currentFrame.getRolls().get(0);
                if (firstRoll + pins > 10) {
                    throw new IllegalArgumentException("Le total des quilles ne peut pas dépasser 10 dans une frame");
                }
            }
        }
        
        // Ajouter le lancer actuel
        currentPlayer.roll(pins);
        
        updateScores();

        // Passe au joueur suivant si le tour est terminé
        if (currentPlayer.isFrameComplete()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            
            // Vérifie si la partie est terminée
            if (currentPlayerIndex == 0) {
                gameOver = true;
                for (Player player : players) {
                    if (!player.isGameComplete()) {
                        gameOver = false;
                        break;
                    }
                }
            }
        }
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isGameOver() {
        // La partie est terminée uniquement si tous les joueurs ont terminé leurs 10 frames
        if (players.isEmpty()) return false;
        
        for (Player player : players) {
            if (!player.isGameComplete()) {
                return false;
            }
        }
        return true;
    }

    private void updateScores() {
        for (Player player : players) {
            List<Frame> frames = player.getFrames();
            
            // Calculer le score de chaque frame
            for (int i = 0; i < frames.size(); i++) {
                Frame frame = frames.get(i);
                int frameScore = 0;
                
                // Somme des quilles tombées dans cette frame
                for (int roll : frame.getRolls()) {
                    if (roll > 0) {  // Ignorer les marqueurs spéciaux
                        frameScore += roll;
                    }
                }
                
                // Mettre à jour le score de la frame
                frame.setScore(frameScore);
            }
        }
    }

    public static class Player {
        private String name;
        private List<Frame> frames;
        private int currentFrame;

        public Frame getCurrentFrame() {
            return frames.get(currentFrame);
        }

        public Player(String name) {
            this.name = name;
            this.frames = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                frames.add(new Frame(i == 9)); // Dernière frame différente
            }
            this.currentFrame = 0;
        }

        public void roll(int pins) {
            // Ne rien faire si le jeu est terminé
            if (isGameComplete()) {
                return;
            }
            
            Frame frame = frames.get(currentFrame);
            frame.addRoll(pins);
            
            // Si la frame actuelle est complète et qu'on n'est pas à la dernière frame, on passe à la suivante
            if (frame.isComplete() && currentFrame < 9) {
                currentFrame++;
            }
        }

        public boolean isFrameComplete() {
            if (currentFrame >= frames.size()) {
                return true;
            }
            Frame current = frames.get(currentFrame);
            
            // Pour toutes les frames, on a besoin de 2 lancers
            // sauf si c'est un strike (10 au premier lancer)
            if (current.getRolls().isEmpty()) {
                return false;
            }
            
            // Si c'est un strike (10 au premier lancer)
            if (current.getRolls().get(0) == 10) {
                return true;
            }
            
            // Sinon, on a besoin de 2 lancers
            return current.getRolls().size() >= 2;
        }

        public boolean isGameComplete() {
            // Le jeu est terminé si on a atteint la 10ème frame et qu'elle est complète
            if (currentFrame >= 10) {
                return true;
            }
            
            // Si on est sur la 10ème frame, on vérifie si elle est complète
            if (currentFrame == 9) {
                Frame lastFrame = frames.get(9);
                // Si c'est un strike, on a besoin d'un seul lancer
                if (!lastFrame.getRolls().isEmpty() && lastFrame.getRolls().get(0) == 10) {
                    return true;
                }
                // Sinon, on a besoin de 2 lancers
                return lastFrame.getRolls().size() >= 2;
            }
            
            return false;
        }

        public String getName() {
            return name;
        }

        public List<Frame> getFrames() {
            return frames;
        }

        public int getTotalScore() {
            if (frames.isEmpty()) {
                return 0;
            }
            
            // Calculer la somme des scores de toutes les frames
            int total = 0;
            for (Frame frame : frames) {
                total += frame.getScore();
            }
            return total;
        }
    }

    public static class Frame {
        private List<Integer> rolls;
        private boolean isLastFrame;
        private int score;

        public boolean isLastFrame() {
            return isLastFrame;
        }
        
        public Frame(boolean isLastFrame) {
            this.rolls = new ArrayList<>();
            this.isLastFrame = isLastFrame;
            this.score = 0;
        }

        public void addRoll(int pins) {
            rolls.add(pins);
        }

        public boolean isComplete() {
            if (isLastFrame) {
                // Dernière frame : 3 lancers si strike ou spare, sinon 2 lancers
                if (rolls.size() >= 3) return true;
                if (rolls.size() == 2) {
                    // Si les deux premiers lancers font 10 ou plus, on a besoin d'un troisième lancer
                    return rolls.get(0) + rolls.get(1) < 10;
                }
                return false;
            } else {
                // Frames normales : complète après un strike ou deux lancers
                return rolls.contains(10) || rolls.size() >= 2;
            }
        }

        public int getFrameScore() {
            return rolls.stream().mapToInt(Integer::intValue).sum();
        }

        public int getScore() {
            return score;
        }
        
        public void setScore(int score) {
            this.score = score;
        }
        
        public boolean isStrike() {
            return !rolls.isEmpty() && rolls.get(0) == 10;
        }
        
        public boolean isSpare() {
            return rolls.size() >= 2 && (rolls.get(0) + rolls.get(1) == 10) && rolls.get(0) != 10;
        }

        public List<Integer> getRolls() {
            return new ArrayList<>(rolls);
        }
    }
}
