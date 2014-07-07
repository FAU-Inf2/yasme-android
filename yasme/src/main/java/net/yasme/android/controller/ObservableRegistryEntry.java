package net.yasme.android.controller;

/**
 * Created by andreas on 27.06.14.
 */
public class ObservableRegistryEntry {

    private FragmentObservable<?, ?> obs;
    private Class fragment;
    private Class param;

    public ObservableRegistryEntry(FragmentObservable<?, ?> obs, Class fragment) {
        this.obs = obs;
        this.fragment = fragment;
    }

    public FragmentObservable<?, ?> getObs() {
        return obs;
    }

    public Class getFragment() {
        return fragment;
    }

    public Class getParam() {
        return param;
    }

    public Boolean check(Class fragmentRef) {
        return fragment.equals(fragmentRef);
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