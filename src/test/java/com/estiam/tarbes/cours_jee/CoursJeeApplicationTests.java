package com.estiam.tarbes.cours_jee;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CoursJeeApplicationTests {

    @Test
    void contextLoads() {
        // Test de chargement du contexte Spring
    }

    @Test
    void testSimpleGame() {
        BowlingGame game = new BowlingGame();
        game.addPlayer("Joueur 1");
        
        // Faire tomber 3 quilles à chaque lancer (20 lancers au total)
        for (int i = 0; i < 20; i++) {
            game.roll(3);
        }
        
        // Vérifier le score total (20 lancers * 3 points = 60 points)
        assertEquals(60, game.getPlayers().get(0).getTotalScore());
    }

    @Test
    void testAddMultiplePlayers() {
        BowlingGame game = new BowlingGame();
        game.addPlayer("Joueur 1");
        game.addPlayer("Joueur 2");
        
        assertEquals(2, game.getPlayers().size());
        assertEquals("Joueur 1", game.getPlayers().get(0).getName());
        assertEquals("Joueur 2", game.getPlayers().get(1).getName());
    }
}
