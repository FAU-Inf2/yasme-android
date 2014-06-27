package net.yasme.android.controller;

import android.app.Fragment;

/**
 * Created by andreas on 27.06.14.
 */
public class ObserverRegistry {

    public static void main(String[] args) {
        ExampleFragment.MyParameters parameters = new ExampleFragment.MyParameters(1, 2.0);
        ObserverRegistry.getRegistry(Observers.EXAMPLEFRAGMENT).notifyFragments(parameters);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Fragment & NotifiableFragment<P>, P extends NotifyFragmentParameter> FragmentObserver1<T, P> getRegistry(Observers observerType) {
        FragmentObserver1<? extends Fragment, ? extends NotifyFragmentParameter> result = null;

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
            default: throw new IllegalStateException("It's broken.");
        }

        return (FragmentObserver1<T, P>) result;
    }

    public enum Observers {

//        CHATFRAGMENT(new FragmentObserver1<ChatFragment>()),
//        USERDETAILS(new FragmentObserver1<UserDetailsFragment>()),
        EXAMPLEFRAGMENT(new FragmentObserver1<ExampleFragment, ExampleFragment.MyParameters>());


        private FragmentObserver1<? extends Fragment, ? extends NotifyFragmentParameter> observer;
        Observers(FragmentObserver1<? extends Fragment, ? extends NotifyFragmentParameter> observer) {
            this.observer = observer;
        }
    }
}
