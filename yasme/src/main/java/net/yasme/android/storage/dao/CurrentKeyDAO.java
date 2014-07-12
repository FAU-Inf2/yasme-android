package net.yasme.android.storage.dao;

import net.yasme.android.entities.MessageKey;
import net.yasme.android.storage.CurrentKey;

import java.util.List;

/**
 * Created by robert on 11.07.14.
 */
public interface CurrentKeyDAO extends DAO<CurrentKey> {

    /**
     * Creates a currentKey item but only if the ID does not already exist in the table. This
     * extracts the id from the parameter, queries for it's id, returning the data if it
     * exists. If it does not exist create will be called with the parameter.
     * @param currentKey to be added
     * @return given currentKey or null if an error occurs
     */
    public CurrentKey addIfNotExists(CurrentKey currentKey);

    /**
     * This method is a combination of addIfNotExists and update for the sake of convenience.
     * Queries for a currentKey item with the specified id. If there is none, addIfNotExists is called,
     * otherwise update.
     * @param currentKey to be added or updated
     * @return given chat after insertion / update
     */
    public CurrentKey addOrUpdate(CurrentKey currentKey);

    public boolean existsCurrentKeyForChat(long chatId);

    /**
     * Retrieves an object associated with a specific ID.
     * @param id Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     */
    public CurrentKey get(long id);

    /**
     * Query for all of the items in the object table
     * @return list of all chats or null on error
     */
    public List<CurrentKey> getAll();

    /**
     * Query for all of the items of one chat in the object table
     * @return list of all currentKey of one chat or null on error
     */
    public List<CurrentKey> getCurrentKeysByChat(long chatId);

    /**
     * Store the fields from an object to the database row corresponding to the id from the
     * data parameter. If you have made changes to an object, this is how you persist those
     * changes to the database. You cannot use this method to update the id field
     * @param currentKey to be updated
     * @return given currentKey or null on error
     */
    public CurrentKey update(CurrentKey currentKey);

    public void updateCurrentKey(long keyId, long chatId);


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * @param currentKey to be deleted
     * @return true on success, false otherwise
     */
    public boolean delete(CurrentKey currentKey);
}
