import java.util.*;
class Node {
    public Node head; // pointer to head of list
    public String value; // data of node (string)
    public Node next; // pointer to next node

    // public function for creating new node
    public Node(String value) {
        this.value = value;
        this.next = null;
    }
}

interface IsingleLinkedList {
    boolean insert(Node node); // inserting node into list (end)
    boolean delete(Node node); // delete param node specified
    String toString(); // printing list w/ conventions ", " and "."
}

public class singleLinkedList implements IsingleLinkedList{

    // function for inserting new node
    public boolean insert(Node node) {
        try {
            // if list empty, insert node at head
            if (node.head == null) {
                node.head = node;
                return true;
            }

            // tmp pointer to traverse list
            Node tmp = node.head;

            // list not empty, find end of list and insert node there
            while (tmp.next != null) {
                tmp = tmp.next; // move tmp through list
            }
            tmp.next = node; // set tmp next to node we want to insert (last node point to new node)
            return true;
        } catch(Exception e) {
            System.out.println("[EX] - " + e);
            return false;
        }
    }

    public boolean delete(Node node) {
        return false;
    }

    // printing list
    public String toString() {
        String out = ""; // string to be returned
        Node tmp = head; // tmp node to move through list
        // loop through all nodes in list and add to out string with ", "
        while (tmp.next != null){
            out += tmp.value + ", ";
        }
        out += tmp.value + "."; // final string added to out string w/ trailing period
        return out; // return out string
    }

    // testing class
    public static void testing() {
        singleLinkedList list = new singleLinkedList();
        list.insert(new Node("this"));
    }

    // main function
    public static void main(String[] args) { testing();}
}
