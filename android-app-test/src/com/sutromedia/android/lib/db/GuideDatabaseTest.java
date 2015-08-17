package com.sutromedia.android.lib.db;


import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


import com.sutromedia.android.lib.db.*;
 
public class GuideDatabaseTest extends AndroidTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        cleanupDatabaseFolder();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        cleanupDatabaseFolder();
    }
    
    private void cleanupDatabaseFolder() {
        File path = getContext().getDatabasePath("doesNotExist");
        File parent = path.getParentFile();
        recursiveDelete(parent);
        assertFalse(parent.exists());
    }
    
    private void recursiveDelete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                recursiveDelete(child);
            }
        } 
        file.delete();
    }
    
    public void testShouldFailWhenOpeningNonExistingAsset() {
        GuideDatabase guide = new GuideDatabase(getContext(), "doesNotExist");
        File path = getContext().getDatabasePath("doesNotExist");
        assertEquals(
            path.getAbsolutePath(), 
            guide.getDatabaseAbsolutePath());
        
        assertFalse(path.exists());
        try {
            guide.createDataBase();
            fail("The resource doesn't exists => creating the db should fail");
        } catch (IOException error) {
            //that's what we expect
            guide.close();
        } catch (SQLException error) {
            fail("Unexpected exception");
        }
    }        

    public void testShouldSucceedWhenOpeningValidAsset() throws IOException, SQLException {
        GuideDatabase guide = new GuideDatabase(getContext(), "content.sqlite3");
        guide.createDataBase();
        File path = new File(guide.getDatabaseAbsolutePath());
        assert(path.exists());
        SQLiteDatabase database = guide.getReadableDatabase();
        assertNotNull(database);
        assertTrue(database.isOpen());
        assertFalse(database.isReadOnly());
        database.close();
        guide.close();
    }
}