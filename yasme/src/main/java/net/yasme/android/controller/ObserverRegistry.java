package net.yasme.android.controller;

import android.app.Fragment;

import net.yasme.android.ui.ChatListFragment;
import net.yasme.android.ui.ContactListItemFragment;
import net.yasme.android.ui.LoginFragment;
import net.yasme.android.ui.RegisterFragment;
import net.yasme.android.ui.SearchContactFragment;
import net.yasme.android.ui.UserDetailsFragment;

/**
 * Created by andreas on 27.06.14.
 */
public class ObserverRegistry {

    public static void main(String[] args) {
        //ObserverRegistry.getRegistry(Observers.EXAMPLEFRAGMENT).register(new ExampleFragment());

        ExampleFragment.MyParameters parameters = new ExampleFragment.MyParameters(1, 2.0);
        ObserverRegistry.getRegistry(Observers.EXAMPLEFRAGMENT).notifyFragments(parameters);
        //Observers.EXAMPLEFRAGMENT.observer.notifyFragments(parameters);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Fragment & NotifiableFragment<P>, P extends NotifyFragmentParameter> FragmentObserver<T, P> getRegistry(Observers observerType) {
        FragmentObserver<? extends Fragment, ? extends NotifyFragmentParameter> result = null;

        switch (observerType) {
//            case CHATFRAGMENT:
//                 result = Observers.CHATFRAGMENT.observer;
//                break;
//            case USERDETAILS:
//                result = Observers.USERDETAILS.observer;
//                break;
            case EXAMPLEFRAGMENT:
                result = Observers.EXAMPLEFRAGMENT.observer;
                break;
            case CHATLISTFRAGMENT:
                result = Observers.CHATLISTFRAGMENT.observer;
                break;
            case REGISTERFRAGMENT:
                result = Observers.REGISTERFRAGMENT.observer;
                break;
            case LOGINFRAGMENT:
                result = Observers.LOGINFRAGMENT.observer;
                break;
            case SEARCHCONTACTFRAGMENT:
                result = Observers.SEARCHCONTACTFRAGMENT.observer;
                break;
            case CONTACTLISTITEMFRAGMENT:
                result = Observers.CONTACTLISTITEMFRAGMENT.observer;
                break;
            case USERDETAILSFRAGMENT:
                result = Observers.USERDETAILSFRAGMENT.observer;
                break;
            default: throw new IllegalStateException("It's broken.");
        }

        return (FragmentObserver<T, P>) result;
    }

    public enum Observers {

//        CHATFRAGMENT(new FragmentObserver1<ChatFragment>()),
//        USERDETAILS(new FragmentObserver1<UserDetailsFragment>()),
        EXAMPLEFRAGMENT(new FragmentObserver<ExampleFragment, ExampleFragment.MyParameters>()),
        CHATLISTFRAGMENT(new FragmentObserver<ChatListFragment, NotifyFragmentParameter>()),
        REGISTERFRAGMENT(new FragmentObserver<RegisterFragment, NotifyFragmentParameter>()),
        LOGINFRAGMENT(new FragmentObserver<LoginFragment, NotifyFragmentParameter>()),
        SEARCHCONTACTFRAGMENT(new FragmentObserver<SearchContactFragment, NotifyFragmentParameter>()),
        CONTACTLISTITEMFRAGMENT(new FragmentObserver<ContactListItemFragment, NotifyFragmentParameter>()),
        USERDETAILSFRAGMENT(new FragmentObserver<UserDetailsFragment, NotifyFragmentParameter>());



        public FragmentObserver<? extends Fragment, ? extends NotifyFragmentParameter> getObserver() {
            return observer;
        }

        private FragmentObserver<? extends Fragment, ? extends NotifyFragmentParameter> observer;

        Observers(FragmentObserver<? extends Fragment, ? extends NotifyFragmentParameter> observer) {
            this.observer = observer;
        }
    }
}
