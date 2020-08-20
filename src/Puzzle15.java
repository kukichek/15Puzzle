import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Puzzle15 extends GridPane {
    private static final int SHUFFLE_STEP_COUNT = 500;

    private MainWindow parent;

    private List<Integer> tilePlaces = new ArrayList<>();
    private ImageView[] tiles;

    private int columnAmount;
    private int tileAmount;
    private int emptyTile;

    private int minHeight;
    private int maxHeight;
    private int imageWidth;
    private int imageHeight;

    private static final int GAP = 2;

    boolean isSolvedShuffle;

    private static String defaultImagePath = "15puzzle.png";
    Image puzzleImage;

    public Puzzle15(MainWindow p, int minH, int maxH) throws IOException {
        parent = p;

        setColumnAmount(4);
        setDefaultImage();

        minHeight = minH;
        maxHeight = maxH - 5 * GAP;

        setVgap(GAP);
        setHgap(GAP);
    }

    public void setDefaultImage() throws IOException {
        setImage(SwingFXUtils.toFXImage(ImageIO.read(new File(defaultImagePath)), null));
    }

    public void setImage(Image image) {
        puzzleImage = image;
        imageWidth = (int) puzzleImage.getWidth();
        imageHeight = (int) puzzleImage.getHeight();
    }

    public void setColumnAmount(int newColumnAmount) {
        columnAmount = newColumnAmount;
        tileAmount = columnAmount * columnAmount;
        emptyTile = tileAmount - 1;

        tiles = new ImageView[tileAmount];
        for (int i = 0; i < tileAmount; ++i) {
            tiles[i] = new ImageView();
        }
    }

    public void setShuffle() {
        int tileW = imageWidth / columnAmount;
        int tileH = imageHeight / columnAmount;

        do {
            tilePlaces = shuffle();
        } while(isSolvedShuffle());

        for (int i = 0; i < tileAmount; ++i) {
            tiles[i].setImage(puzzleImage);
            tiles[i].setPreserveRatio(true);

            if (imageHeight > maxHeight) { tiles[i].setFitHeight(maxHeight / columnAmount); }
            if (imageHeight < minHeight) { tiles[i].setFitHeight(minHeight / columnAmount); }

            Rectangle2D rect = new Rectangle2D((i % columnAmount) * tileW, (i / columnAmount) * tileH, tileW, tileH);
            tiles[i].setViewport(rect);

            int tileIndex = i;
            isSolvedShuffle = false;
            tiles[i].setOnMouseClicked(e -> {
                if (isEmptyTileNeighbour(tileIndex) && !isSolvedShuffle) {
                    Collections.swap(tilePlaces, tileIndex, emptyTile);
                    getChildren().remove(tiles[tileIndex]);
                    getChildren().remove(tiles[emptyTile]);
                    add(tiles[tileIndex], tilePlaces.get(tileIndex) % columnAmount, tilePlaces.get(tileIndex) / columnAmount);
                    add(tiles[emptyTile], tilePlaces.get(emptyTile) % columnAmount, tilePlaces.get(emptyTile) / columnAmount);

                    if (isSolvedShuffle()) {
                        isSolvedShuffle = true;
                        parent.showWinPanel();
                    }
                }
            });
        }

        Rectangle2D rect = new Rectangle2D(-tileW, -tileH, tileW, tileH);
        tiles[emptyTile].setViewport(rect);

        getChildren().clear();
        for (int i = 0; i < tileAmount; ++i) {
            add(tiles[i], tilePlaces.get(i) % columnAmount, tilePlaces.get(i) / columnAmount);
        }
    }

    private boolean isSolvedShuffle () {
        for (int i = 0; i < tileAmount; ++i) {
            if (tilePlaces.get(i) != i) {
                return false;
            }
        }
        return true;
    }

    private boolean isEmptyTileNeighbour(int tileNumber) {
        return (Math.abs(tilePlaces.get(tileNumber) - tilePlaces.get(emptyTile)) == 1) ||
                (Math.abs(tilePlaces.get(tileNumber) - tilePlaces.get(emptyTile)) == columnAmount);
    }

    private List<Integer> shuffle() {
        /**
         * maps indexes of tiles in game field to indexes of tiles in solved game
         */
        int[] realToSolvedTalesPlaces = new int[tileAmount];

        /**
         * maps indexes of tiles in solved game to indexes of tiles in game field
         */
        int[] solvedToRealTalesPlaces = new int[tileAmount];

        int[][] move = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        Random random = new Random(new Date().getTime());

        /**
         * initiates game field
         */
        for (int i = 0; i < tileAmount; ++i) {
            realToSolvedTalesPlaces[i] = i;
            solvedToRealTalesPlaces[i] = i;
        }

        for (int i = 0; i < SHUFFLE_STEP_COUNT; ++i) {
            int realEmptyTileIndex = solvedToRealTalesPlaces[emptyTile];

            int moveIndex;
            do {
                moveIndex = random.nextInt(move.length);
            } while(!isCorrectMove(realEmptyTileIndex, move[moveIndex]));

            int realIndexOfTileToSwap = realEmptyTileIndex + move[moveIndex][0] * columnAmount + move[moveIndex][1];

            int temp = realToSolvedTalesPlaces[realEmptyTileIndex];
            realToSolvedTalesPlaces[realEmptyTileIndex] = realToSolvedTalesPlaces[realIndexOfTileToSwap];
            realToSolvedTalesPlaces[realIndexOfTileToSwap] = temp;

            solvedToRealTalesPlaces[realToSolvedTalesPlaces[realEmptyTileIndex]] = realEmptyTileIndex;
            solvedToRealTalesPlaces[realToSolvedTalesPlaces[realIndexOfTileToSwap]] = realIndexOfTileToSwap;
        }

        List<Integer> tilePlaces = new ArrayList<>();
        for (int place : solvedToRealTalesPlaces) {
            tilePlaces.add(place);
        }

        return tilePlaces;
    }

    private boolean isCorrectMove (int currentPlace, int[] move) {
        int i = currentPlace / columnAmount;
        int j = currentPlace % columnAmount;

        i += move[0];
        j += move[1];

        return (i >= 0) && (i < columnAmount) && (j >= 0) && (j < columnAmount);
    }
}
