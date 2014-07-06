package net.yasme.android.controller;

import android.app.Fragment;

import net.yasme.android.ui.ChatListFragment;

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
            case CHATLISTFRAGMENT:
                result = Observers.CHATLISTFRAGMENT.observer;
                break;
            case REGISTERFRAGMENT:
                result = Observers.REGISTERFRAGMENT.observer;
                break;
            case LOGINFRAGMENT:
                result = Observers.LOGINFRAGMENT.observer;
                break;
            case EXAMPLEFRAGMENT:
                  result = Observers.EXAMPLEFRAGMENT.observer;
                break;
            default: throw new IllegalStateException("It's broken.");
        }

        return (FragmentObserver<T, P>) result;
    }

    public enum Observers {

//        CHATFRAGMENT(new FragmentObserver1<ChatFragment>()),
//        USERDETAILS(new FragmentObserver1<UserDetailsFragment>()),
        CHATLISTFRAGMENT(new FragmentObserver<ChatListFragment, NotifyFragmentParameter>()),
        REGISTERFRAGMENT(new FragmentObserver<ChatListFragment, NotifyFragmentParameter>()),
        LOGINFRAGMENT(new FragmentObserver<ChatListFragment, NotifyFragmentParameter>()),
        EXAMPLEFRAGMENT(new FragmentObserver<ExampleFragment, ExampleFragment.MyParameters>());


        public FragmentObserver<? extends Fragment, ? extends NotifyFragmentParameter> getObserver() {
            return observer;
        }

        private FragmentObserver<? extends Fragment, ? extends NotifyFragmentParameter> observer;

        Observers(FragmentObserver<? extends Fragment, ? extends NotifyFragmentParameter> observer) {
            this.observer = observer;
        }
    }
}
