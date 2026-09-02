-- Géneros base
INSERT INTO genres (name) VALUES
('Acción'), ('Drama'), ('Comedia'), ('Terror'), ('Ciencia ficción'),
('Animación'), ('Thriller'), ('Romance'), ('Fantasía'), ('Documental');

-- Películas de ejemplo (posters vía picsum.photos con seed fija para reproducibilidad)
INSERT INTO movies (title, synopsis, release_year, rating, poster_url, backdrop_url, trailer_url, media_type) VALUES
('Ecos del Mañana', 'Un ingeniero descubre una anomalía temporal que amenaza con reescribir el presente.', 2024, 8.2, 'https://picsum.photos/seed/nextpick1/400/600', 'https://picsum.photos/seed/nextpick1/1280/720', '', 'MOVIE'),
('La Última Frontera', 'Una tripulación espacial se enfrenta a decisiones imposibles al perder contacto con la Tierra.', 2023, 7.6, 'https://picsum.photos/seed/nextpick2/400/600', 'https://picsum.photos/seed/nextpick2/1280/720', '', 'MOVIE'),
('Sombras de Medianoche', 'En un pueblo aislado, los habitantes empiezan a desaparecer tras la caída del sol.', 2022, 6.9, 'https://picsum.photos/seed/nextpick3/400/600', 'https://picsum.photos/seed/nextpick3/1280/720', '', 'MOVIE'),
('Risas Compartidas', 'Dos desconocidos atrapados en un aeropuerto forjan una amistad inesperada.', 2021, 7.1, 'https://picsum.photos/seed/nextpick4/400/600', 'https://picsum.photos/seed/nextpick4/1280/720', '', 'MOVIE'),
('El Peso del Silencio', 'Un drama íntimo sobre una familia que reconstruye su relación tras una tragedia.', 2020, 8.5, 'https://picsum.photos/seed/nextpick5/400/600', 'https://picsum.photos/seed/nextpick5/1280/720', '', 'MOVIE'),
('Cazadores de Sombra', 'Un detective con habilidades sobrenaturales investiga crímenes imposibles de explicar.', 2024, 7.3, 'https://picsum.photos/seed/nextpick6/400/600', 'https://picsum.photos/seed/nextpick6/1280/720', '', 'MOVIE'),
('Mundo de Cristal', 'Una animación sobre un reino frágil que debe unirse ante una amenaza invisible.', 2023, 8.0, 'https://picsum.photos/seed/nextpick7/400/600', 'https://picsum.photos/seed/nextpick7/1280/720', '', 'MOVIE'),
('Corazones en Fuga', 'Una historia de amor que atraviesa fronteras y decisiones difíciles.', 2019, 6.8, 'https://picsum.photos/seed/nextpick8/400/600', 'https://picsum.photos/seed/nextpick8/1280/720', '', 'MOVIE');

-- Series de ejemplo
INSERT INTO movies (title, synopsis, release_year, rating, poster_url, backdrop_url, trailer_url, media_type) VALUES
('Redes Ocultas', 'Un thriller tecnológico sobre una periodista que destapa una conspiración corporativa.', 2024, 8.4, 'https://picsum.photos/seed/nextpick9/400/600', 'https://picsum.photos/seed/nextpick9/1280/720', '', 'SERIES'),
('Reino de Cenizas', 'Fantasía épica sobre la caída y resurgir de una dinastía milenaria.', 2022, 8.9, 'https://picsum.photos/seed/nextpick10/400/600', 'https://picsum.photos/seed/nextpick10/1280/720', '', 'SERIES'),
('Vecinos Extraños', 'Comedia sobre una comunidad de vecinos que esconden secretos absurdos.', 2023, 7.0, 'https://picsum.photos/seed/nextpick11/400/600', 'https://picsum.photos/seed/nextpick11/1280/720', '', 'SERIES'),
('Estación Cero', 'Ciencia ficción sobre los últimos supervivientes de una estación espacial abandonada.', 2021, 7.8, 'https://picsum.photos/seed/nextpick12/400/600', 'https://picsum.photos/seed/nextpick12/1280/720', '', 'SERIES'),
('Anima: Guardianes', 'Serie de animación sobre jóvenes guerreros que protegen un mundo espiritual.', 2024, 8.1, 'https://picsum.photos/seed/nextpick13/400/600', 'https://picsum.photos/seed/nextpick13/1280/720', '', 'SERIES'),
('El Archivo', 'Un documental por entregas sobre casos criminales sin resolver.', 2020, 7.4, 'https://picsum.photos/seed/nextpick14/400/600', 'https://picsum.photos/seed/nextpick14/1280/720', '', 'SERIES');

-- Tags para carruseles (Tendencias, Top 10, Animes...)
INSERT INTO movie_tags (movie_id, tag)
SELECT id, 'tendencias' FROM movies WHERE title IN ('Ecos del Mañana', 'Redes Ocultas', 'Cazadores de Sombra', 'Reino de Cenizas');

INSERT INTO movie_tags (movie_id, tag)
SELECT id, 'top10' FROM movies WHERE title IN ('El Peso del Silencio', 'Reino de Cenizas', 'Redes Ocultas', 'Mundo de Cristal', 'Ecos del Mañana');

INSERT INTO movie_tags (movie_id, tag)
SELECT id, 'animes' FROM movies WHERE title IN ('Mundo de Cristal', 'Anima: Guardianes');

INSERT INTO movie_tags (movie_id, tag)
SELECT id, 'destacado' FROM movies WHERE title = 'Reino de Cenizas';

-- Relación películas-géneros
INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Ecos del Mañana' AND g.name IN ('Ciencia ficción', 'Thriller');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'La Última Frontera' AND g.name IN ('Ciencia ficción', 'Drama');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Sombras de Medianoche' AND g.name IN ('Terror');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Risas Compartidas' AND g.name IN ('Comedia', 'Romance');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'El Peso del Silencio' AND g.name IN ('Drama');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Cazadores de Sombra' AND g.name IN ('Thriller', 'Fantasía');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Mundo de Cristal' AND g.name IN ('Animación', 'Fantasía');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Corazones en Fuga' AND g.name IN ('Romance', 'Drama');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Redes Ocultas' AND g.name IN ('Thriller', 'Drama');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Reino de Cenizas' AND g.name IN ('Fantasía', 'Acción');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Vecinos Extraños' AND g.name IN ('Comedia');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Estación Cero' AND g.name IN ('Ciencia ficción');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Anima: Guardianes' AND g.name IN ('Animación', 'Acción');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'El Archivo' AND g.name IN ('Documental');
