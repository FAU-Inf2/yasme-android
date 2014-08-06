package net.yasme.android.storage.dao;

import net.yasme.android.entities.MessageKey;

import java.util.List;

/**
 * Created by robert on 11.07.14.
 */
public interface MessageKeyDAO extends DAO<MessageKey> {

    /**
     * Creates a messageKey item but only if the ID does not already exist in the table. This
     * extracts the id from the parameter, queries for it's id, returning the data if it
     * exists. If it does not exist create will be called with the parameter.
     * @param messageKey to be added
     * @return given messageKey or null if an error occurs
     */
    public MessageKey addIfNotExists(MessageKey messageKey);


    /**
     * This method is a combination of addIfNotExists and update for the sake of convenience.
     * Queries for a messageKey item with the specified id. If there is none, addIfNotExists is called,
     * otherwise update.
     * @param messageKey to be added or updated
     * @return given chat after insertion / update
     */
    public MessageKey addOrUpdate(MessageKey messageKey);

    /**
     * Retrieves an object associated with a specific ID.
     * @param id Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     */
    public MessageKey get(long id);

    /**
     * Query for all of the items in the object table
     * @return list of all chats or null on error
     */
    public List<MessageKey> getAll();

    /**
     * Query for all of the items of one chat in the object table
     * @return list of all messageKeys of one chat or null on error
     */
    public List<MessageKey> getMessageKeysByChat(long chatId);

    /**
     * Returns the latest key for the specific chat
     * @return latest key for the specific chat
     */
    public MessageKey getCurrentKeyByChat(long chatId);

    /**
     * Store the fields from an object to the database row corresponding to the id from the
     * data parameter. If you have made changes to an object, this is how you persist those
     * changes to the database. You cannot use this method to update the id field
     * @param messageKey to be updated
     * @return given messageKey or null on error
     */
    public MessageKey update(MessageKey messageKey);


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * @param messageKey to be deleted
     * @return true on success, false otherwise
     */
    public boolean delete(MessageKey messageKey);
}
