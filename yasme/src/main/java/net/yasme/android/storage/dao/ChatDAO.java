package net.yasme.android.storage.dao;

import net.yasme.android.entities.Chat;

import java.util.List;

/**
 * Created by bene on 11.07.14.
 */
public interface ChatDAO {

    /**
     * Creates a chat item but only if the ID does not already exist in the table. This
     * extracts the id from the chat parameter, queries for it's id, returning the data if it
     * exists. If it does not exist create will be called with the parameter.
     * @param chat to be added
     * @return given chat or null if an error occurs
     */
    public Chat add(Chat chat);

    /**
     * Retrieves an object associated with a specific ID.
     * @param id Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     */
    public Chat get(long id);

    /**
     * Query for all of the items in the object table
     * @return list of all chats or null on error
     */
    public List<Chat> getAll();


    /**
     * Store the fields from an object to the database row corresponding to the id from the data parameter. If you have
     * made changes to an object, this is how you persist those changes to the database. You cannot use this method to
     * update the id field
     * @param chat to be updated
     * @return given chat or null on error
     */
    public Chat update(Chat chat);


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * @param chat to be deleted
     * @return true on success, false otherwise
     */
    public boolean delete(Chat chat);
}
