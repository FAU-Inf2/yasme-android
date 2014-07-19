package net.yasme.android.storage.dao;

import net.yasme.android.storage.RSAKey;

import java.util.List;

/**
 * Created by robert on 19.07.14.
 */
public interface RSAKeyDAO {

    /**
     * Creates a rsaKey item but only if the ID does not already exist in the table. This
     * extracts the id from the parameter, queries for it's id, returning the data if it
     * exists. If it does not exist create will be called with the parameter.
     * @param rsaKey to be added
     * @return given rsaKey or null if an error occurs
     */
    public RSAKey addIfNotExists(RSAKey rsaKey);


    /**
     * This method is a combination of addIfNotExists and update for the sake of convenience.
     * Queries for a rsaKey item with the specified id. If there is none, addIfNotExists is called,
     * otherwise update.
     * @param rsaKey to be added or updated
     * @return given chat after insertion / update
     */
    public RSAKey addOrUpdate(RSAKey rsaKey);

    /**
     * Retrieves an object associated with a specific ID.
     * @param id Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     */
    public RSAKey get(long id);

    /**
     * Query for all of the items in the object table
     * @return list of all rsaKeys or null on error
     */
    public List<RSAKey> getAll();

    /**
     * Query for all of the items of one chat in the object table
     * @return list of all rsaKeys of one chat or null on error
     */
    public List<RSAKey> getMessageKeysByDevice(long chatId);


    /**
     * Store the fields from an object to the database row corresponding to the id from the
     * data parameter. If you have made changes to an object, this is how you persist those
     * changes to the database. You cannot use this method to update the id field
     * @param rsaKey to be updated
     * @return given rsaKey or null on error
     */
    public RSAKey update(RSAKey rsaKey);


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * @param rsaKey to be deleted
     * @return true on success, false otherwise
     */
    public boolean delete(RSAKey rsaKey);
}
