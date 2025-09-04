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
        if (!currentFrame.isLastFrame() && currentFrame.getRolls().size() == 1) {
            int firstRoll = currentFrame.getRolls().get(0);
            if (firstRoll + pins > 10) {
                throw new IllegalArgumentException("Le total des quilles ne peut pas dépasser 10 dans une frame");
            }
        }
        
        // Gestion des strikes et spares
        if (pins == 10 && !currentFrame.isLastFrame() && currentFrame.getRolls().isEmpty()) {
            // Strike
            currentPlayer.roll(10);
            if (!currentFrame.isLastFrame()) {
                currentPlayer.roll(-2); // Marqueur pour strike (sera ignoré dans le calcul)
            }
        } else if (!currentFrame.getRolls().isEmpty() && 
                  currentFrame.getRolls().get(0) + pins == 10 && 
                  !currentFrame.isLastFrame()) {
            // Spare
            currentPlayer.roll(pins);
            currentPlayer.roll(-1); // Marqueur pour spare
        } else {
            // Lancer normal
            currentPlayer.roll(pins);
        }
        
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
            int score = 0;
            List<Frame> frames = player.getFrames();
            
            for (int i = 0; i < frames.size(); i++) {
                Frame frame = frames.get(i);
                int frameScore = 0;
                List<Integer> rolls = frame.getRolls();
                
                // Calcul du score de base de la frame (sans les bonus)
                for (int roll : rolls) {
                    if (roll > 0) {  // Ignorer les marqueurs spéciaux (-1, -2)
                        frameScore += roll;
                    }
                }
                
                // Gestion des bonus pour les strikes et spares
                if (i < 9) {  // Pas de bonus pour la 10ème frame
                    if (rolls.size() > 0 && rolls.get(0) == 10) {  // Strike
                        // Chercher les 2 prochains lancers valides
                        int bonus = 0;
                        int count = 0;
                        
                        // Parcourir les frames suivantes pour trouver les 2 prochains lancers
                        for (int j = i + 1; j < frames.size() && count < 2; j++) {
                            for (int roll : frames.get(j).getRolls()) {
                                if (roll > 0) {  // Ignorer les marqueurs spéciaux
                                    bonus += roll;
                                    count++;
                                    if (count == 2) break;
                                }
                            }
                        }
                        
                        frameScore += bonus;
                    } 
                    // Vérifier si c'est un spare (somme des 2 premiers lancers = 10, et pas un strike)
                    else if (rolls.size() >= 2 && rolls.get(0) + rolls.get(1) == 10 && rolls.get(0) != 10) {
                        // Chercher le prochain lancer valide
                        if (i < 8) {  // Pour les frames 1 à 8
                            for (int j = i + 1; j < frames.size(); j++) {
                                for (int roll : frames.get(j).getRolls()) {
                                    if (roll > 0) {  // Premier lancer valide trouvé
                                        frameScore += roll;
                                        j = frames.size();  // Sortir de la boucle
                                        break;
                                    }
                                }
                            }
                        } else if (i == 8) {  // Pour la 9ème frame
                            Frame tenthFrame = frames.get(9);
                            if (!tenthFrame.getRolls().isEmpty()) {
                                frameScore += tenthFrame.getRolls().get(0);
                            }
                        }
                    }
                }
                
                score += frameScore;
                frame.setScore(score);
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
            
            // Pour les frames 0-8 (les 9 premières frames)
            if (currentFrame < 9) {
                // Si strike, la frame est complète
                if (current.getRolls().contains(10)) {
                    return true;
                }
                // Si deux lancers, la frame est complète
                return current.getRolls().size() >= 2;
            } 
            // Pour la dernière frame (index 9)
            else {
                // Si moins de 2 lancers, la frame n'est pas complète
                if (current.getRolls().size() < 2) {
                    return false;
                }
                // Si les deux premiers lancers font moins de 10, pas de troisième lancer
                int firstTwoRolls = current.getRolls().get(0) + current.getRolls().get(1);
                if (firstTwoRolls < 10) {
                    return true;
                }
                // Sinon, besoin d'un troisième lancer
                return current.getRolls().size() >= 3;
            }
        }

        public boolean isGameComplete() {
            // Le jeu est terminé si on a atteint la dernière frame et qu'elle est complète
            if (currentFrame >= 10) {
                return true;
            }
            
            // Vérifier si la dernière frame est complète
            if (currentFrame == 9) {
                Frame lastFrame = frames.get(9);
                // Dernière frame complète si 2 lancers normaux ou 3 lancers si strike ou spare
                if (lastFrame.getRolls().size() >= 3) {
                    return true;
                } else if (lastFrame.getRolls().size() == 2) {
                    // Si pas de strike ou spare dans la dernière frame, 2 lancers suffisent
                    return lastFrame.getRolls().get(0) + lastFrame.getRolls().get(1) < 10;
                }
                return false;
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
            return frames.stream().mapToInt(Frame::getScore).sum();
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
