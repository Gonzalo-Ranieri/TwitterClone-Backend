package com.twitterclone.backend.config;

import com.twitterclone.backend.model.Follow;
import com.twitterclone.backend.model.Like;
import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.Tweet;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.FollowRepository;
import com.twitterclone.backend.repository.LikeRepository;
import com.twitterclone.backend.repository.TweetRepository;
import com.twitterclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Base de datos ya poblada. Omitiendo la generación de seed data.");
            return;
        }

        log.info("Poblando la base de datos con seed data realista...");

        String password = passwordEncoder.encode("password123");
        List<User> users = new ArrayList<>();

        // Create 10 users
        String[][] userData = {
            {"juan_perez", "juan@example.com", "Desarrollador Java & Spring Boot fan. Hablo de código y café."},
            {"maria_coder", "maria@example.com", "React & Frontend Engineer. Amante del clean code y el diseño UI/UX."},
            {"tech_guru", "guru@example.com", "Opiniones sobre IA, Cloud Computing y el futuro de la tecnología."},
            {"antigravity", "anti@example.com", "An AI coding assistant here to help. Built by the Google DeepMind team."},
            {"messi10", "messi@example.com", "El mejor del mundo. Sólo juego al fútbol."},
            {"chef_luis", "luis@example.com", "Cocinero apasionado. Compartiendo recetas sencillas y deliciosas."},
            {"fit_lucia", "lucia@example.com", "Entrenadora personal. Ayudando a construir hábitos saludables."},
            {"gamer_pro", "gamer@example.com", "Streamer a tiempo parcial. Hablo de videojuegos retro y novedades."},
            {"viajera_eli", "eli@example.com", "Viajando por el mundo con una mochila. 42 países visitados."},
            {"cinefilo_max", "max@example.com", "Crítico de cine amateur. Hilos sobre películas infravaloradas."}
        };

        for (String[] data : userData) {
            User user = User.builder()
                    .username(data[0])
                    .email(data[1])
                    .password(password)
                    .bio(data[2])
                    .avatarPlaceholder("avatar_" + data[0] + ".png")
                    .roles(Set.of(Role.USER))
                    .build();
            users.add(userRepository.save(user));
        }

        // Create tweets (at least 2 to 3 per user)
        List<Tweet> tweets = new ArrayList<>();
        String[][] tweetsData = {
            {"juan_perez", "¡Mi primer tweet! Acabo de implementar el seeder de base de datos en Spring Boot."},
            {"juan_perez", "El problema N+1 en Hibernate es un clásico. Usar proyecciones DTO ayuda un montón."},
            {"maria_coder", "Diseñar interfaces bonitas con CSS puro es terapéutico. ¿Quién necesita Tailwind?"},
            {"maria_coder", "Hoy aprendí cómo implementar infinite scroll en React. ¡Es súper interactivo!"},
            {"tech_guru", "La inteligencia artificial no reemplaza a los programadores, sino a los que no la usan."},
            {"tech_guru", "Docker Compose facilita la vida al levantar bases de datos locales para desarrollo."},
            {"antigravity", "Hello world! Estoy listo para ayudar en el clon de Twitter/X. ¡Vamos a programar!"},
            {"antigravity", "Recuerden escribir pruebas unitarias para todas las restricciones de su modelo."},
            {"messi10", "¡Qué lindo volver a entrenar con la selección! Siempre es un orgullo."},
            {"messi10", "Gracias a todos por el cariño de siempre. Se viene un partido importante."},
            {"chef_luis", "El secreto para una buena salsa boloñesa está en la cocción lenta: mínimo 2 horas."},
            {"chef_luis", "Hoy sale asado. El fuego ya está listo, ¿quién trae las bebidas?"},
            {"fit_lucia", "No busques motivación, busca disciplina. La disciplina te lleva a donde la motivación falla."},
            {"fit_lucia", "Mantenerse hidratado es fundamental para la recuperación muscular. ¡Tomen agua!"},
            {"gamer_pro", "Terminando de jugar al Chrono Trigger por décima vez. Qué obra de arte."},
            {"gamer_pro", "Se viene la nueva consola. ¿Ustedes qué opinan? ¿Vale la pena comprarla de salida?"},
            {"viajera_eli", "Amaneciendo en el salar de Uyuni, Bolivia. Las palabras no alcanzan para describirlo."},
            {"viajera_eli", "Consejo viajero: siempre lleva una batería portátil y copia física de tu pasaporte."},
            {"cinefilo_max", "Viendo de nuevo Interestelar de Nolan. La banda sonora de Hans Zimmer es sublime."},
            {"cinefilo_max", "Un hilo rápido sobre directores que merecen más reconocimiento. Abro hilo..."}
        };

        for (String[] tData : tweetsData) {
            String username = tData[0];
            String content = tData[1];
            User author = users.stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst()
                    .orElse(users.get(0));

            Tweet tweet = Tweet.builder()
                    .content(content)
                    .author(author)
                    .build();
            tweets.add(tweetRepository.save(tweet));
            // Tiny delay to guarantee chronological sorting works properly
            Thread.sleep(10);
        }

        // Set up cross-follows (every user follows at least 3-4 other users)
        Random random = new Random();
        for (User follower : users) {
            int followsCount = 0;
            // Try to follow up to 4 other users
            while (followsCount < 4) {
                User following = users.get(random.nextInt(users.size()));
                if (!follower.getId().equals(following.getId()) &&
                    !followRepository.existsByFollowerAndFollowing(follower, following)) {
                    
                    Follow follow = Follow.builder()
                            .follower(follower)
                            .following(following)
                            .build();
                    followRepository.save(follow);
                    followsCount++;
                }
            }
        }

        // Set up cross-likes (each tweet gets up to 4 likes)
        for (Tweet tweet : tweets) {
            int likesCount = 0;
            int targetLikes = random.nextInt(5); // 0 to 4 likes
            while (likesCount < targetLikes) {
                User liker = users.get(random.nextInt(users.size()));
                if (!likeRepository.existsByUserAndTweet(liker, tweet)) {
                    Like like = Like.builder()
                            .user(liker)
                            .tweet(tweet)
                            .build();
                    likeRepository.save(like);
                    likesCount++;
                }
            }
        }

        log.info("Base de datos poblada exitosamente con: {} usuarios, {} tweets, {} follows y {} likes.",
                userRepository.count(), tweetRepository.count(), followRepository.count(), likeRepository.count());
    }
}
