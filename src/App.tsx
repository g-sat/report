import { useState, useEffect } from 'react'
import './App.css'

interface Item {
  id: number;
  name: string;
  quantity: number;
  price: number;
  total: number;
}

function App() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [newItem, setNewItem] = useState({ name: '', quantity: 0, price: 0 });

  // Fetch items from backend
  const fetchItems = async () => {
    try {
      const response = await fetch('/api/items');
      if (response.ok) {
        const data = await response.json();
        setItems(data);
      } else {
        console.error('Failed to fetch items');
      }
    } catch (error) {
      console.error('Error fetching items:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, []);

  const addItem = async () => {
    if (newItem.name && newItem.quantity > 0 && newItem.price > 0) {
      try {
        const response = await fetch('/api/items', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: newItem.name,
            quantity: newItem.quantity,
            price: newItem.price
          })
        });
        
        if (response.ok) {
          await fetchItems(); // Refresh the list
          setNewItem({ name: '', quantity: 0, price: 0 });
        } else {
          alert('Failed to add item');
        }
      } catch (error) {
        console.error('Error adding item:', error);
        alert('Error adding item');
      }
    }
  };

  const removeItem = async (id: number) => {
    try {
      const response = await fetch(`/api/items/${id}`, {
        method: 'DELETE'
      });
      
      if (response.ok) {
        await fetchItems(); // Refresh the list
      } else {
        alert('Failed to remove item');
      }
    } catch (error) {
      console.error('Error removing item:', error);
      alert('Error removing item');
    }
  };

  const getGrandTotal = () => {
    return items.reduce((sum, item) => sum + (item.quantity * item.price), 0);
  };

  const generateReport = async () => {
    try {
      const res = await fetch('/api/reports/generate');
      if (!res.ok) throw new Error('Failed to generate report');
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'inventory-report.pdf';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
      alert('Could not download report. Is the backend running on port 8080?');
    }
  };

  if (loading) {
    return <div className="app"><h2>Loading...</h2></div>;
  }

  return (
    <div className="app">
      <h1>Sales Inventory Management</h1>
      
      {/* Add New Item Form */}
      <div className="add-item-form">
        <h3>Add New Item</h3>
        <div className="form-row">
          <input
            type="text"
            placeholder="Item name"
            value={newItem.name}
            onChange={(e) => setNewItem({...newItem, name: e.target.value})}
          />
          <input
            type="number"
            placeholder="Quantity"
            value={newItem.quantity || ''}
            onChange={(e) => setNewItem({...newItem, quantity: parseInt(e.target.value) || 0})}
          />
          <input
            type="number"
            step="0.01"
            placeholder="Price"
            value={newItem.price || ''}
            onChange={(e) => setNewItem({...newItem, price: parseFloat(e.target.value) || 0})}
          />
          <button onClick={addItem}>Add Item</button>
        </div>
      </div>

      {/* Data Table */}
      <div className="table-container">
        <h3>Inventory Items</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Item Name</th>
              <th>Quantity</th>
              <th>Price ($)</th>
              <th>Total ($)</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.id}</td>
                <td>{item.name}</td>
                <td>{item.quantity}</td>
                <td>${item.price.toFixed(2)}</td>
                <td>${(item.quantity * item.price).toFixed(2)}</td>
                <td>
                  <button 
                    className="remove-btn"
                    onClick={() => removeItem(item.id)}
                  >
                    Remove
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={4}><strong>Grand Total:</strong></td>
              <td><strong>${getGrandTotal().toFixed(2)}</strong></td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>

      {/* Report Generation */}
      <div className="report-section">
        <h3>Generate Report</h3>
        <p>Click the button below to generate a PDF report from the database data.</p>
        <button className="generate-report-btn" onClick={generateReport}>
          📊 Generate PDF Report from Database
        </button>
      </div>
    </div>
  )
}

export default App
