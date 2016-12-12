package ru.spbau.mit.CreationAndConfigs;

public enum VaryingParameter {
    ELEMS_PER_REQ {
        @Override
        public String toString() {
            return "Elements per request";
        }
    },
    CLIENTS_PARALLEL {
        @Override
        public String toString() {
            return "Clients at the same time";
        }
    },
    TIME_DELTA {
        @Override
        public String toString() {
            return "Time before next request";
        }
    }
}
