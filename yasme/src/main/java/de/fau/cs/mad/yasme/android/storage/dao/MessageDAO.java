package de.fau.cs.mad.yasme.android.storage.dao;

import de.fau.cs.mad.yasme.android.entities.Message;

import java.util.List;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 11.07.14.
 */
public interface MessageDAO extends DAO<Message> {
    /**
     * Creates a message but only if the ID does not already exist in the table. This
     * extracts the id from the chat parameter, queries for it's id, returning the data if it
     * exists. If it does not exist create will be called with the parameter.
     * @param message to be added
     * @return given chat or null if an error occurs
     */
    public Message addIfNotExists(Message message);

    /**
     * Retrieves an object associated with a specific ID.
     * @param id Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     */
    public Message get(long id);

    /**
     * Query for all of the message items of a given chat. The returned messages are ordered ascending by their id
     * @return list of all messages of one chat or null on error
     */
    public List<Message> getNewMessagesByChat(long chatId, long latestMessageId);

    public Message getNewestMessageOfChat(long chatId);

    /**
     * Store the fields from an object to the database row corresponding to the id from the data
     * parameter. If you have made changes to an object, this is how you persist those changes
     * to the database. You cannot use this method to update the id field
     * @param message to be updated
     * @return given chat or null on error
     */
    public Message update(Message message);


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * @param message to be deleted
     * @return true on success, otherwise false
     */
    public boolean delete(Message message);
}
