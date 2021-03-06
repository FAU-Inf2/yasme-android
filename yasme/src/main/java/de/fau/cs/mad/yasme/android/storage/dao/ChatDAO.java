package de.fau.cs.mad.yasme.android.storage.dao;

import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;

import java.util.List;
import java.util.Set;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 11.07.14.
 */
public interface ChatDAO extends DAO<Chat> {

    /**
     * Creates a chat item but only if the ID does not already exist in the table. This
     * extracts the id from the chat parameter, queries for it's id, returning the data if it
     * exists. If it does not exist create will be called with the parameter.
     * @param chat to be added
     * @return given chat or null if an error occurs
     */
    public Chat addIfNotExists(Chat chat);


    /**
     * This method is a combination of addIfNotExists and update for the sake of convenience.
     * Queries for a chat item with the specified id. If there is none, addIfNotExists is called, otherwise update.
     * @param chat to be added or updated
     * @return given chat after insertion / update
     */
    public Chat addOrUpdate(Chat chat);

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
     * Retrieves all chats from the database which are held between the exact list of given users
     * @param users who participate in the chat to return. Don't forget that you are a participant as well and thus should be present in the list
     * @return list of chats which matched the conditions
     */
    public List<Chat> getByParticipantsExact(Set<User> users);


    /**
     * Retrieves all chats from the database which are held between the given users or more
     * @param users who participate in the chat to look for
     * @return list of chats which have the given users (and more) as participants
     */
    public List<Chat> getByTheseParticipantsOrMore(Set<User> users);

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


    /**
     * Replaces all currently stored chats with given ones. Updates old chats with the values from the new one if their id matches.
     * @param newChats to be stored
     * @return true if successful, false otherwise
     */
    public boolean refreshAll(List<Chat> newChats);
}
