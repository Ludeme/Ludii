Generated with:

SELECT Games.Name, Categories.Category FROM Games, Categories, GameCategories WHERE Games.id = GameCategories.GameId AND Categories.Id = GameCategories.CategoryId