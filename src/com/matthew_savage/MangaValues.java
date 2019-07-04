package com.matthew_savage;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;

import static com.matthew_savage.CategoryMangaLists.*;

public class MangaValues {

    // need to add some kind of method to autofill tablename.

    public static void changeStatus(boolean isComplete, String dbFileName) {
        if (isComplete) {
            currentParentList.get(parentListIndexNumberTEMP).setStatus(Values.DB_COL_STATUS_VAL_DONE.getValue());
            currentContent.get(currentContentListIndexNumberTEMP).setStatus(Values.DB_COL_STATUS_VAL_DONE.getValue());
        } else {
            currentParentList.get(parentListIndexNumberTEMP).setStatus(Values.DB_COL_STATUS_VAL_NOT_DONE.getValue());
            currentContent.get(currentContentListIndexNumberTEMP).setStatus(Values.DB_COL_STATUS_VAL_NOT_DONE.getValue());
        }
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_STATUS.getValue(), currentParentList.get(parentListIndexNumberTEMP).getStatus());
        //move manga to or from the correct db. clearly need to call close db sepertely just for the situation above.
    }

    public static void changeWebAddress(String webAddress, String dbFileName) {
        currentParentList.get(parentListIndexNumberTEMP).setWebAddress(webAddress);
        currentContent.get(currentContentListIndexNumberTEMP).setWebAddress(webAddress);
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_URL.getValue(), webAddress);
    }

    public static void changeTotalchapters(int totalChapters, String dbFileName) {
        currentParentList.get(parentListIndexNumberTEMP).setTotalChapters(totalChapters);
        currentContent.get(currentContentListIndexNumberTEMP).setTotalChapters(totalChapters);
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_CHAP_TOT.getValue(), totalChapters);
    }

    public static void changeCurrentPageNumber(int currentPageNumber, String dbFileName) {
        currentParentList.get(parentListIndexNumberTEMP).setCurrentPage(currentPageNumber);
        currentContent.get(currentContentListIndexNumberTEMP).setCurrentPage(currentPageNumber);
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_CUR_PAGE.getValue(), currentPageNumber);
    }

    public static void changeLastChapterRead(int lastChapterRead, String dbFileName) {
        currentParentList.get(parentListIndexNumberTEMP).setLastChapterRead(lastChapterRead);
        currentContent.get(currentContentListIndexNumberTEMP).setLastChapterRead(lastChapterRead);
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_LAST_CHAP_READ.getValue(), lastChapterRead);
    }

    public static void changeLastChapterDownloaded(int lastChapterDownloaded, String dbFileName) {
        currentParentList.get(parentListIndexNumberTEMP).setLastChapterRead(lastChapterDownloaded);
        currentContent.get(currentContentListIndexNumberTEMP).setLastChapterRead(lastChapterDownloaded);
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_LAST_CHAP_DL.getValue(), lastChapterDownloaded);
    }

    public static void changeNewChaptersFlag(boolean hasNewChapters, String dbFileName) {
        currentParentList.get(parentListIndexNumberTEMP).setNewChapters(hasNewChapters);
        currentContent.get(currentContentListIndexNumberTEMP).setNewChapters(hasNewChapters);
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_NEW_CHAP_BOOL.getValue(), hasNewChapters);
    }

    public static void changeFavoriteFlag(boolean isFavorited, String dbFileName) {
        currentParentList.get(parentListIndexNumberTEMP).setFavorite(isFavorited);
        currentContent.get(currentContentListIndexNumberTEMP).setFavorite(isFavorited);
        Database.accessDb(dbFileName);
        Database.modifyManga(selectedMangaIdentNumberTEMP, Values.DB_COL_FAVE_BOOL.getValue(), isFavorited);
    }

    public static void setBookmark(String dbFileName) {
        bookmark.set(0, currentContent.get(currentContentListIndexNumberTEMP));
        Database.accessDb(dbFileName);
        Database.createBookmark(Values.DB_TABLE_BOOKMARK.getValue(),
                selectedMangaIdentNumberTEMP,
                selectedMangaTotalChapNumTEMP,
                selectedMangaLastChapReadNumTEMP,
                selectedMangaCurrentPageNumTEMP);
        Database.terminateDbAccess();

    }

    public static void addToHistory(String dbFileName) {
        System.out.println("adding to history");
        history.add(currentContent.get(currentContentListIndexNumberTEMP));
        try {
            Database.accessDb(dbFileName);
            PreparedStatement preparedStatement = Database.dbConnection.prepareStatement(
                    "INSERT INTO history (title_id, title, summary) VALUES (?,?,?)");
            preparedStatement.setInt(1, selectedMangaIdentNumberTEMP);
            preparedStatement.setString(2, selectedMangaTitleTEMP);
            preparedStatement.setString(3, selectedMangaSummaryTEMP);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Database.terminateDbAccess();
        }
    }

    public static void setFiveLatestMangas(ArrayList<MangaArrayList> arrayList) {

            try {
                Database.accessDb(Values.DB_NAME_MANGA.getValue());
                PreparedStatement delete = Database.dbConnection.prepareStatement("DELETE FROM newest_manga");
                PreparedStatement insert = Database.dbConnection.prepareStatement("INSERT INTO newest_manga (title_id, title) VALUES (?,?)");

                Voucher.acquire();
                delete.execute();
                Voucher.release();
                delete.close();

                for (int i = 0; i < 5; i++) {
                    insert.setInt(1, arrayList.get(i).getTitleId());
                    insert.setString(2, arrayList.get(i).getTitle());
                    insert.addBatch();
                }

                Voucher.acquire();
                insert.executeBatch();
                insert.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Database.terminateDbAccess();
                Voucher.release();
            }
    }

    public static void addNewTitles(ArrayList<MangaArrayList> arrayList) {
        try {
            Database.accessDb(Values.DB_NAME_MANGA.getValue());
            PreparedStatement addTitle = Database.dbConnection.prepareStatement("" +
                    "INSERT INTO available_manga (title_id, " +
                    "title, " +
                    "authors, " +
                    "status, " +
                    "summary, " +
                    "web_address, " +
                    "genre_tags, " +
                    "total_chapters, " +
                    "current_page, " +
                    "last_chapter_read, " +
                    "last_chapter_downloaded, " +
                    "new_chapters, " +
                    "favorite) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");

            for (MangaArrayList manga : arrayList) {
                addTitle.setInt(1, manga.getTitleId());
                addTitle.setString(2, manga.getTitle());
                addTitle.setString(3, manga.getAuthors());
                addTitle.setString(4, manga.getStatus());
                addTitle.setString(5, manga.getSummary());
                addTitle.setString(6, manga.getWebAddress());
                addTitle.setString(7, manga.getGenreTags());
                addTitle.setInt(8, 0);
                addTitle.setInt(9, 0);
                addTitle.setInt(10, 0);
                addTitle.setInt(11, 0);
                addTitle.setInt(12, 0);
                addTitle.setInt(13, 0);
                addTitle.addBatch();
            }

            Voucher.acquire();
            addTitle.executeBatch();
            addTitle.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Database.terminateDbAccess();
            Voucher.release();
        }
    }
}