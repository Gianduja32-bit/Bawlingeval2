package com.estiam.tarbes.cours_jee;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@SessionAttributes("game")
public class BowlingController {

    @ModelAttribute("game")
    public BowlingGame game() {
        BowlingGame game = new BowlingGame();
        // Ajouter un joueur par défaut
        game.addPlayer("Joueur 1");
        return game;
    }

    @GetMapping("/")
    public String home(Model model) {
        BowlingGame game = new BowlingGame();
        game.addPlayer("Joueur 1");
        updateModel(model, game);
        return "bowling";
    }

    @PostMapping("/roll")
    public String roll(@RequestParam("pins") int pins, 
                      @ModelAttribute("game") BowlingGame game, 
                      Model model) {
        System.out.println("=== NOUVEAU LANCER ===");
        System.out.println("Pins reçus: " + pins);
        
        try {
            // Valider l'entrée
            if (pins < 0 || pins > 10) {
                throw new IllegalArgumentException("Le nombre de quilles doit être entre 0 et 10");
            }
            
            // Effectuer le lancer
            game.roll(pins);
            System.out.println("Lancer effectué avec succès");
            model.addAttribute("success", true);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
            model.addAttribute("error", "Une erreur inattendue est survenue: " + e.getMessage());
        }
        
        // Mettre à jour le modèle et réafficher la page
        updateModel(model, game);
        return "bowling";
    }

    @PostMapping("/reset")
    public String reset() {
        return "redirect:/";
    }

    private void updateModel(Model model, BowlingGame game) {
        model.addAttribute("players", game.getPlayers());
        model.addAttribute("currentPlayerIndex", game.getCurrentPlayerIndex());
        model.addAttribute("gameOver", game.isGameOver());
        model.addAttribute("game", game);
        
        // Log pour le débogage
        System.out.println("=== ÉTAT DU JEU ===");
        System.out.println("Joueur actuel: " + game.getCurrentPlayerIndex());
        System.out.println("Jeu terminé: " + game.isGameOver());
        if (!game.getPlayers().isEmpty()) {
            System.out.println("Score total: " + game.getPlayers().get(0).getTotalScore());
        }
    }
}
