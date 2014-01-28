package com.companyx.android.cookingxp;

import com.companyx.android.cookingxp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * BaseActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class BaseActivity extends Activity {
	// SYSTEM
	protected RecipeDatabase recipeDatabase;
	protected GameData gameData;
	protected SharedPreferences.Editor sharedPrefEditor;
	protected float dpiScalingFactor;
	
	private void init() {
		recipeDatabase = RecipeDatabase.getInstance(this);
		gameData = GameData.getInstance(this);
		sharedPrefEditor = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
		dpiScalingFactor = getResources().getDisplayMetrics().density;
		
		// enable "type-to-search", activates the search dialog when the user starts typing on the keyboard
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
		case R.id.menu_recipes:
			intent = new Intent(this, SelectRecipeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra("operation", "Categories");
			startActivity(intent);
			break;
		case R.id.menu_trees:
			intent = new Intent(this, TreeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
		case R.id.menu_shopping_list:
			intent = new Intent(this, SelectRecipeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra("operation", "Shopping List");
			startActivity(intent);
			break;
		case R.id.menu_favorites:
			intent = new Intent(this, SelectRecipeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra("operation", "Favorites");
			startActivity(intent);
			break;
		case R.id.menu_search:
			onSearchRequested();
			break;
		case R.id.menu_settings:
			// TODO
			break;
		case R.id.menu_adam:
			intent = new Intent(this, AdamActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
		case R.id.menu_quit:
			intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
