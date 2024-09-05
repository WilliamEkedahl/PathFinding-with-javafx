// PROG2 VT24, Inlämningsuppgift, del 2
// Grupp 159
// Max Lindberg mali7984
// William Ekedahl wiek0904
// Simon Lundqvist silu8199

import java.util.*;
import java.util.LinkedList;

public class ListGraph<T> implements Graph<T>{
    private Map<T, Set<Edge<T>>> graph = new HashMap<>();


    public void add(T node){
        Set<Edge<T>> setEdge = new HashSet<>();
        if (!graph.containsKey(node)){
            graph.put(node, setEdge);
        }
    }

    public void remove(T node){
        if (graph.containsKey(node)){
            Set<Edge<T>> edges = graph.get(node);
            Iterator<Edge<T>> iter = edges.iterator();
            while (iter.hasNext()){
                Edge<T> tmpEdge = iter.next();
                Iterator<Edge<T>> backIter = graph.get(tmpEdge.getDestination()).iterator();
                while (backIter.hasNext()){
                    Edge<T> backEdge = backIter.next();
                    if (backEdge.getDestination().equals(node)){
                        backIter.remove();
                    }
                }iter.remove();
            }graph.remove(node);
        }else {
            throw new NoSuchElementException();
        }
    }

    public void connect(T node1, T node2, String name, int weight){
        if (!graph.containsKey(node1) || !graph.containsKey(node2)){
            throw new NoSuchElementException();
        } else if (weight < 0) {
            throw new IllegalArgumentException();
        } else if (getEdgeBetween(node1, node2) != null) {
            throw new IllegalStateException();
        }else {
            graph.get(node1).add(new Edge<>(node2, weight, name));
            graph.get(node2).add(new Edge<>(node1, weight, name));
        }

    }
    public Edge<T> getEdgeBetween(T node1, T node2) {

        if (!graph.containsKey(node1)) {
            throw new NoSuchElementException("Node1 " + node1 + " does not exist");
        }
        if (!graph.containsKey(node2)){
            throw new NoSuchElementException("Node2 " + node2 + " does not exist");
        }
        for (Edge<T> tmpEdge : graph.get(node1)){
            if (tmpEdge.getDestination().equals(node2)){
                return tmpEdge;
            }
        }return null;
    }

    public void disconnect(T node1, T node2){
        if(!graph.containsKey(node1) || !graph.containsKey(node2)){
            throw new NoSuchElementException();
        } else if (getEdgeBetween(node1, node2) == null) {
            throw new IllegalStateException();
        }else{
            graph.get(node1).remove(getEdgeBetween(node1, node2));
            graph.get(node2).remove(getEdgeBetween(node2, node1));
        }
    }
    public void setConnectionWeight(T node1, T node2, int x){
        if(!graph.containsKey(node1) || !graph.containsKey(node2)){// added negation
            throw new NoSuchElementException();
        } else if (x<0) {
            throw new IllegalArgumentException();
        }
        getEdgeBetween(node1, node2).setWeight(x);
        getEdgeBetween(node2, node1).setWeight(x);
    }

    @Override
    public Set<T> getNodes(){
        return Collections.unmodifiableSet(graph.keySet());
    }

    public Set<Edge<T>> getEdgesFrom(T node){
        if (graph.get(node) == null) {
            throw new NoSuchElementException();
        }else {
            return Collections.unmodifiableSet(graph.get(node));
        }
    }
    public String toString(){
        StringBuilder sb = new StringBuilder("Cities\n");
        for (T tmp : graph.keySet()){
            sb.append(tmp.toString() + "\n");
            for(Edge<T> tmpEdge : graph.get(tmp)){
                sb.append(tmpEdge.toString() + "\n");
            }
        }
        return sb.toString();
    }
    public boolean pathExists(T node1, T node2){
        if (getPath(node1, node2) == null){
            return false;
        }else return !getPath(node1, node2).isEmpty();
    }

    public List<Edge<T>> getPath(T from, T to){
        Map<T, T> connection = new HashMap<>();
        recursiveConnect(from, null, connection);
        LinkedList<Edge<T>> path = new LinkedList<>();
        T current = to;
        while (current != null && !current.equals(from)){
            if (connection.get(current) != null){
                T next = connection.get(current);
                Edge<T> edge = getEdgeBetween(next, current);
                path.addFirst(edge);
                current= next;
            }else return null;
        }
        return path;
    }

    private void recursiveConnect(T to, T from, Map<T, T> connection){
        connection.put(to, from);
        if (graph.get(to) != null){
            for(Edge<T> edge : graph.get(to)){
                if(!connection.containsKey(edge.getDestination())){
                    recursiveConnect(edge.getDestination(), to, connection);
                }
            }
        }
    }
}
