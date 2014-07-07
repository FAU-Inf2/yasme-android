package net.yasme.android.controller;

import android.app.Fragment;
import android.util.Log;

import net.yasme.android.ui.ChatListFragment;
import net.yasme.android.ui.ContactListItemFragment;
import net.yasme.android.ui.LoginFragment;
import net.yasme.android.ui.RegisterFragment;
import net.yasme.android.ui.SearchContactFragment;
import net.yasme.android.ui.UserDetailsFragment;

import java.util.ArrayList;

/**
 * Created by andreas on 27.06.14.
 */
public class ObserverRegistry {

    private static ArrayList<ObserverRegistryEntry> entries = new ArrayList<>();

    public static <T extends NotifiableFragment<P>, P> FragmentObserver<T, P> getObservable(Class fragmentClass, Class paramClass) {
        for (ObserverRegistryEntry entry : entries) {
           if (entry.check(fragmentClass,paramClass)) {
               Log.d("ObserverRegistry","Returned existing observable");
               return (FragmentObserver<T,P>)entry.getObs();
           }
        }
        FragmentObserver<T, P> res = new FragmentObserver<T, P>();
        ObserverRegistryEntry entry = new ObserverRegistryEntry(res,fragmentClass,paramClass);
        Log.d("ObserverRegistry","Created new observable");
        entries.add(entry);
        return res;
    }
}
    /*
    public static void main(String[] args) {
        //ObserverRegistry.getRegistry(Observers.EXAMPLEFRAGMENT).register(new ExampleFragment());

        ExampleFragment.MyParameters parameters = new ExampleFragment.MyParameters(1, 2.0);
        ObserverRegistry.getRegistry(Observers.EXAMPLEFRAGMENT).notifyFragments(parameters);
        //Observers.EXAMPLEFRAGMENT.observer.notifyFragments(parameters);
        FragmentObserver<ChatListFragment,ChatListFragment.ChatListParam> ob = new FragmentObserver<>();
        ob.register(new ChatListFragment());
    }

    @SuppressWarnings("unchecked")
    public static <T extends NotifiableFragment<P>, P> FragmentObserver<T, P> getRegistry(Observers observerType) {
        FragmentObserver<? extends Fragment, ?> result = null;

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
        CHATLISTFRAGMENT(new FragmentObserver<ChatListFragment, ChatListFragment.ChatListParam>()),
        REGISTERFRAGMENT(new FragmentObserver<RegisterFragment, NotifyFragmentParameter>()),
        LOGINFRAGMENT(new FragmentObserver<LoginFragment, NotifyFragmentParameter>()),
        SEARCHCONTACTFRAGMENT(new FragmentObserver<SearchContactFragment, NotifyFragmentParameter>()),
        CONTACTLISTITEMFRAGMENT(new FragmentObserver<ContactListItemFragment, NotifyFragmentParameter>()),
        USERDETAILSFRAGMENT(new FragmentObserver<UserDetailsFragment, NotifyFragmentParameter>());



        public FragmentObserver<T extends NotifiableFragment<P>, P> getObserver() {
            return observer;
        }

        private FragmentObserver<T extends NotifiableFragment<P>, P> observer;

        Observers(FragmentObserver<T extends NotifiableFragment<P>, P> observer) {
            this.observer = observer;
        }
    }
}

*/